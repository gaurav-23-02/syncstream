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
public class TrackDto {
    private String id;
    private String title;
    private String mainArtist;
    private List<String> artists;
    private String album;
    private Long durationMs;
    private String isrc;
    private String imageUrl;
    private String spotifyUri;
    private String previewUrl;

    // Matching metadata
    private String matchedYoutubeVideoId;
    private String matchedYoutubeTitle;
    private Double matchConfidence;
    private boolean fromCache;
    private String status; // "PENDING", "MATCHED", "FAILED", "INSERTED"
}
