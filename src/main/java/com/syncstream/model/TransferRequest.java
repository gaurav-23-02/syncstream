package com.syncstream.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private String spotifyPlaylistId;
    private String targetPlaylistName;
    private String targetPlaylistDescription;
    private String privacyStatus; // "private", "unlisted", "public"
    private boolean useFastParallel; // Whether to parallelize search
}
