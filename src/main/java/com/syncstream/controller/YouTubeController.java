package com.syncstream.controller;

import com.syncstream.service.YouTubeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YouTubeController {

    private final YouTubeService youTubeService;

    @PostMapping("/playlists")
    public ResponseEntity<Map<String, String>> createPlaylist(@RequestBody CreatePlaylistRequest request) {
        String playlistId = youTubeService.createPlaylist(
                request.getTitle(),
                request.getDescription(),
                request.getPrivacyStatus()
        );
        return ResponseEntity.ok(Map.of(
                "playlistId", playlistId,
                "url", "https://youtube.com/playlist?list=" + playlistId
        ));
    }

    @Data
    public static class CreatePlaylistRequest {
        private String title;
        private String description;
        private String privacyStatus;
    }
}
