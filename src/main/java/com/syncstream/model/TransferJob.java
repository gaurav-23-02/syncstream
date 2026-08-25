package com.syncstream.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferJob {
    private String jobId;
    private String spotifyPlaylistId;
    private String spotifyPlaylistName;
    private String spotifyPlaylistImageUrl;
    private String targetPlaylistName;
    private String targetPrivacyStatus;
    private String status; // "PENDING", "IN_PROGRESS", "COMPLETED", "FAILED"

    private int totalTracks;
    private int processedTracks;
    private int matchedTracks;
    private int failedTracks;
    private int cacheHits;
    private int progressPercentage;

    private String youtubePlaylistId;
    private String youtubePlaylistUrl;

    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private String errorMessage;

    @Builder.Default
    private List<TrackDto> tracks = new CopyOnWriteArrayList<>();

    @Builder.Default
    private List<TransferLogEntry> logs = new CopyOnWriteArrayList<>();

    @JsonIgnore
    @Builder.Default
    private transient List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addLog(String level, String message, String details) {
        TransferLogEntry entry = TransferLogEntry.builder()
                .id(String.valueOf(System.nanoTime()))
                .timestamp(Instant.now())
                .level(level)
                .message(message)
                .details(details)
                .build();
        logs.add(entry);
    }
}
