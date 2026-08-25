package com.syncstream.controller;

import com.syncstream.model.PlaylistDto;
import com.syncstream.model.TrackDto;
import com.syncstream.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;

    @GetMapping("/playlists")
    public ResponseEntity<List<PlaylistDto>> getPlaylists() {
        List<PlaylistDto> playlists = spotifyService.getUserPlaylists();
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/playlists/{playlistId}/tracks")
    public ResponseEntity<List<TrackDto>> getPlaylistTracks(@PathVariable("playlistId") String playlistId) {
        List<TrackDto> tracks = spotifyService.getPlaylistTracks(playlistId);
        return ResponseEntity.ok(tracks);
    }
}
