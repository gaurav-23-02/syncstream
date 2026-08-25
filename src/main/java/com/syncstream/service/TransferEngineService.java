package com.syncstream.service;

import com.syncstream.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class TransferEngineService {

    private final SpotifyService spotifyService;
    private final YouTubeService youTubeService;
    private final TrackMatchingCacheService cacheService;
    private final ExecutorService virtualThreadExecutor;

    private final Map<String, TransferJob> jobStore = new ConcurrentHashMap<>();
    private final List<TransferJob> jobHistory = new CopyOnWriteArrayList<>();

    public TransferEngineService(
            SpotifyService spotifyService,
            YouTubeService youTubeService,
            TrackMatchingCacheService cacheService,
            @Qualifier("virtualThreadExecutor") ExecutorService virtualThreadExecutor) {
        this.spotifyService = spotifyService;
        this.youTubeService = youTubeService;
        this.cacheService = cacheService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    public TransferJob startTransfer(TransferRequest request) {
        String jobId = "job_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        String targetName = (request.getTargetPlaylistName() != null && !request.getTargetPlaylistName().isBlank())
                ? request.getTargetPlaylistName()
                : "SyncStream - " + request.getSpotifyPlaylistId();

        TransferJob job = TransferJob.builder()
                .jobId(jobId)
                .spotifyPlaylistId(request.getSpotifyPlaylistId())
                .targetPlaylistName(targetName)
                .targetPrivacyStatus(request.getPrivacyStatus() != null ? request.getPrivacyStatus() : "private")
                .status("PENDING")
                .totalTracks(0)
                .processedTracks(0)
                .matchedTracks(0)
                .failedTracks(0)
                .cacheHits(0)
                .progressPercentage(0)
                .createdAt(Instant.now())
                .tracks(new CopyOnWriteArrayList<>())
                .logs(new CopyOnWriteArrayList<>())
                .emitters(new CopyOnWriteArrayList<>())
                .build();

        job.addLog("INFO", "Transfer job initialized", "Job ID: " + jobId);
        jobStore.put(jobId, job);
        jobHistory.add(0, job);

        // Dispatch transfer execution onto Java 21 Virtual Threads
        virtualThreadExecutor.submit(() -> executeTransfer(job, request));

        return job;
    }

    public SseEmitter subscribe(String jobId) {
        TransferJob job = jobStore.get(jobId);
        if (job == null) {
            log.warn("Attempt to subscribe to non-existent transfer job: {}", jobId);
            SseEmitter dummy = new SseEmitter(10_000L);
            try {
                dummy.send(SseEmitter.event().name("error").data("Job not found: " + jobId));
                dummy.complete();
            } catch (IOException ignored) {}
            return dummy;
        }

        // Set generous timeout for the SSE stream (30 minutes)
        SseEmitter emitter = new SseEmitter(1800_000L);
        job.getEmitters().add(emitter);

        emitter.onCompletion(() -> job.getEmitters().remove(emitter));
        emitter.onTimeout(() -> job.getEmitters().remove(emitter));
        emitter.onError((e) -> job.getEmitters().remove(emitter));

        // Immediately transmit the current state and logs to the newly connected subscriber
        try {
            TransferProgressEvent initEvent = buildProgressEvent(job, "Connected to live transfer stream", "INFO");
            emitter.send(SseEmitter.event().name("progress").data(initEvent, MediaType.APPLICATION_JSON));

            // If job already concluded, send final complete event
            if ("COMPLETED".equals(job.getStatus()) || "FAILED".equals(job.getStatus())) {
                emitter.send(SseEmitter.event().name("complete").data(initEvent, MediaType.APPLICATION_JSON));
                emitter.complete();
            }
        } catch (IOException e) {
            job.getEmitters().remove(emitter);
        }

        return emitter;
    }

    public Optional<TransferJob> getJob(String jobId) {
        return Optional.ofNullable(jobStore.get(jobId));
    }

    public List<TransferJob> getAllJobs() {
        return new ArrayList<>(jobHistory);
    }

    private void executeTransfer(TransferJob job, TransferRequest request) {
        Instant startTime = Instant.now();
        job.setStartedAt(startTime);
        job.setStatus("IN_PROGRESS");

        try {
            job.addLog("INFO", "Fetching tracks from Spotify...", "Playlist ID: " + request.getSpotifyPlaylistId());
            broadcastProgress(job, "Connecting to Spotify & fetching track metadata...", "INFO");

            // 1. Retrieve tracks from Spotify
            List<TrackDto> tracks = spotifyService.getPlaylistTracks(request.getSpotifyPlaylistId());
            job.setTracks(new CopyOnWriteArrayList<>(tracks));
            job.setTotalTracks(tracks.size());

            if (tracks.isEmpty()) {
                job.setStatus("FAILED");
                job.setErrorMessage("Spotify playlist is empty or could not be retrieved.");
                job.addLog("ERROR", "No tracks found in playlist", request.getSpotifyPlaylistId());
                broadcastProgress(job, "Transfer aborted: playlist is empty.", "ERROR");
                return;
            }

            job.addLog("SUCCESS", "Fetched " + tracks.size() + " tracks from Spotify", "Ready to create YouTube destination");
            broadcastProgress(job, "Fetched " + tracks.size() + " tracks. Creating YouTube playlist...", "SUCCESS");

            // Small delay for smooth UI transition
            Thread.sleep(300);

            // 2. Create YouTube Destination Playlist
            job.addLog("INFO", "Creating target YouTube playlist: '" + job.getTargetPlaylistName() + "'", "Privacy: " + job.getTargetPrivacyStatus());
            String ytPlaylistId = youTubeService.createPlaylist(
                    job.getTargetPlaylistName(),
                    request.getTargetPlaylistDescription() != null ? request.getTargetPlaylistDescription() : "Transferred from Spotify using SyncStream",
                    job.getTargetPrivacyStatus()
            );

            job.setYoutubePlaylistId(ytPlaylistId);
            job.setYoutubePlaylistUrl("https://youtube.com/playlist?list=" + ytPlaylistId);
            job.addLog("SUCCESS", "YouTube playlist created successfully", "ID: " + ytPlaylistId);
            broadcastProgress(job, "Target YouTube playlist ready. Starting track matching & sync...", "SUCCESS");

            // 3. Process each track with Virtual Thread parallelism / pipelining
            int total = tracks.size();
            for (int i = 0; i < total; i++) {
                TrackDto track = tracks.get(i);
                int trackNum = i + 1;

                job.addLog("INFO", "Processing track [" + trackNum + "/" + total + "]: " + track.getTitle(), track.getMainArtist());

                // Match track to YouTube Video ID
                TrackDto matched = youTubeService.matchTrack(track);

                if (matched.isFromCache()) {
                    job.setCacheHits(job.getCacheHits() + 1);
                    job.addLog("SUCCESS", "Cache HIT for '" + track.getTitle() + "' -> Video: " + matched.getMatchedYoutubeVideoId(), "Saved YouTube search quota");
                } else {
                    job.addLog("INFO", "Searched & matched '" + track.getTitle() + "' -> Video: " + matched.getMatchedYoutubeVideoId(), "Confidence: " + (int)(matched.getMatchConfidence() * 100) + "%");
                }

                // Insert into YouTube playlist
                boolean inserted = youTubeService.insertPlaylistItem(ytPlaylistId, matched.getMatchedYoutubeVideoId());
                if (inserted) {
                    matched.setStatus("INSERTED");
                    job.setMatchedTracks(job.getMatchedTracks() + 1);
                } else {
                    matched.setStatus("FAILED");
                    job.setFailedTracks(job.getFailedTracks() + 1);
                }

                job.setProcessedTracks(trackNum);
                int progressPercent = (int) (((double) trackNum / total) * 100);
                job.setProgressPercentage(progressPercent);

                // Broadcast current step
                String msg = String.format("Synced (%d/%d): %s - %s", trackNum, total, track.getMainArtist(), track.getTitle());
                broadcastProgress(job, msg, "INFO", track, trackNum);

                // Small pacing delay for realistic streaming animation (between 150ms and 300ms)
                Thread.sleep(220);
            }

            // 4. Conclude Transfer Job
            job.setStatus("COMPLETED");
            job.setCompletedAt(Instant.now());
            job.setProgressPercentage(100);

            long totalDurationSec = Duration.between(startTime, job.getCompletedAt()).toSeconds();
            String summary = String.format("Transfer complete! Synced %d/%d tracks in %ds (%d cache hits).",
                    job.getMatchedTracks(), total, totalDurationSec, job.getCacheHits());

            job.addLog("SUCCESS", summary, "Playlist URL: " + job.getYoutubePlaylistUrl());
            broadcastProgress(job, summary, "SUCCESS");
            broadcastComplete(job);

            log.info("Job {} completed successfully in {}s. Synced {}/{} tracks.", job.getJobId(), totalDurationSec, job.getMatchedTracks(), total);

        } catch (Exception e) {
            log.error("Transfer job {} encountered an unhandled error: {}", job.getJobId(), e.getMessage(), e);
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            job.addLog("ERROR", "Transfer failed: " + e.getMessage(), e.getClass().getSimpleName());
            broadcastProgress(job, "Transfer failed: " + e.getMessage(), "ERROR");
        }
    }

    private void broadcastProgress(TransferJob job, String message, String logLevel) {
        broadcastProgress(job, message, logLevel, null, job.getProcessedTracks());
    }

    private void broadcastProgress(TransferJob job, String message, String logLevel, TrackDto currentTrack, int currentTrackIndex) {
        TransferProgressEvent event = buildProgressEvent(job, message, logLevel);
        if (currentTrack != null) {
            event.setCurrentTrackIndex(currentTrackIndex);
            event.setCurrentTrackTitle(currentTrack.getTitle());
            event.setCurrentArtist(currentTrack.getMainArtist());
            event.setCurrentAlbum(currentTrack.getAlbum());
            event.setMatchedVideoId(currentTrack.getMatchedYoutubeVideoId());
            event.setMatchedVideoTitle(currentTrack.getMatchedYoutubeTitle());
            event.setFromCache(currentTrack.isFromCache());
        }

        for (SseEmitter emitter : job.getEmitters()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(event, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                job.getEmitters().remove(emitter);
            }
        }
    }

    private void broadcastComplete(TransferJob job) {
        TransferProgressEvent event = buildProgressEvent(job, "Transfer completed successfully!", "SUCCESS");
        for (SseEmitter emitter : job.getEmitters()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(event, MediaType.APPLICATION_JSON));
                emitter.complete();
            } catch (Exception e) {
                job.getEmitters().remove(emitter);
            }
        }
    }

    private TransferProgressEvent buildProgressEvent(TransferJob job, String message, String logLevel) {
        long elapsed = (job.getStartedAt() != null)
                ? Duration.between(job.getStartedAt(), Instant.now()).toMillis()
                : 0;

        return TransferProgressEvent.builder()
                .jobId(job.getJobId())
                .progress(job.getProgressPercentage())
                .status(job.getStatus())
                .totalTracks(job.getTotalTracks())
                .currentTrackIndex(job.getProcessedTracks())
                .matchedCount(job.getMatchedTracks())
                .failedCount(job.getFailedTracks())
                .cacheHitCount(job.getCacheHits())
                .youtubePlaylistId(job.getYoutubePlaylistId())
                .youtubePlaylistUrl(job.getYoutubePlaylistUrl())
                .message(message)
                .log(message)
                .logLevel(logLevel)
                .elapsedMillis(elapsed)
                .recentTracks(job.getTracks() != null ? new ArrayList<>(job.getTracks()) : Collections.emptyList())
                .build();
    }
}
