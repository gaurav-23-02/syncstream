package com.syncstream.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferProgressEvent {
    private String jobId;
    private int progress; // 0 - 100
    private String status; // "PENDING", "IN_PROGRESS", "COMPLETED", "FAILED"
    private int currentTrackIndex;
    private int totalTracks;
    private String currentTrackTitle;
    private String currentArtist;
    private String currentAlbum;
    private String matchedVideoId;
    private String matchedVideoTitle;
    private boolean fromCache;
    private String message;
    private String log;
    private String logLevel; // "INFO", "SUCCESS", "WARN", "ERROR"
    private String youtubePlaylistId;
    private String youtubePlaylistUrl;
    private int matchedCount;
    private int failedCount;
    private int cacheHitCount;
    private long elapsedMillis;
    private List<TrackDto> recentTracks;
}
