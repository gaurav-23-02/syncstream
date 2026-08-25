package com.syncstream.service;

import com.syncstream.model.PlaylistDto;
import com.syncstream.model.TrackDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final SessionAuthService sessionAuthService;

    /**
     * Fetch user's Spotify playlists (handling pagination up to 100 items).
     */
    public List<PlaylistDto> getUserPlaylists() {
        String token = sessionAuthService.getSpotifyAccessToken();
        if (token == null || token.startsWith("demo_") || token.startsWith("mock_")) {
            log.info("Returning rich sample Spotify playlists (Demo Mode)");
            return getDemoPlaylists();
        }

        try {
            SpotifyApi spotifyApi = new SpotifyApi.Builder()
                    .setAccessToken(token)
                    .build();

            List<PlaylistDto> result = new ArrayList<>();
            int limit = 50;
            int offset = 0;
            boolean hasMore = true;

            while (hasMore && offset < 100) {
                Paging<PlaylistSimplified> paging = spotifyApi.getListOfCurrentUsersPlaylists()
                        .limit(limit)
                        .offset(offset)
                        .build()
                        .execute();

                if (paging == null || paging.getItems() == null || paging.getItems().length == 0) {
                    break;
                }

                for (PlaylistSimplified item : paging.getItems()) {
                    String imageUrl = (item.getImages() != null && item.getImages().length > 0)
                            ? item.getImages()[0].getUrl()
                            : "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=300&auto=format&fit=crop&q=80";

                    result.add(PlaylistDto.builder()
                            .id(item.getId())
                            .title(item.getName())
                            .description("Spotify playlist with " + (item.getTracks() != null ? item.getTracks().getTotal() : 0) + " tracks")
                            .owner(item.getOwner() != null ? item.getOwner().getDisplayName() : "Spotify User")
                            .totalTracks(item.getTracks() != null ? item.getTracks().getTotal() : 0)
                            .imageUrl(imageUrl)
                            .uri(item.getUri())
                            .isPublic(item.getIsPublicAccess())
                            .collaborative(item.getIsCollaborative())
                            .build());
                }

                offset += limit;
                hasMore = paging.getNext() != null && result.size() < paging.getTotal();
            }

            return result;
        } catch (Exception e) {
            log.warn("Error fetching real Spotify playlists (falling back to sample playlists): {}", e.getMessage());
            return getDemoPlaylists();
        }
    }

    /**
     * Fetch all tracks for a specific Spotify playlist.
     */
    public List<TrackDto> getPlaylistTracks(String playlistId) {
        String token = sessionAuthService.getSpotifyAccessToken();
        if (token == null || token.startsWith("demo_") || token.startsWith("mock_") || playlistId.startsWith("demo_")) {
            return getDemoPlaylistTracks(playlistId);
        }

        try {
            SpotifyApi spotifyApi = new SpotifyApi.Builder()
                    .setAccessToken(token)
                    .build();

            List<TrackDto> trackList = new ArrayList<>();
            int limit = 100;
            int offset = 0;
            boolean hasMore = true;

            while (hasMore && offset < 300) {
                Paging<PlaylistTrack> playlistTrackPaging = spotifyApi.getPlaylistsItems(playlistId)
                        .limit(limit)
                        .offset(offset)
                        .build()
                        .execute();

                if (playlistTrackPaging == null || playlistTrackPaging.getItems() == null) {
                    break;
                }

                for (PlaylistTrack pt : playlistTrackPaging.getItems()) {
                    if (pt.getTrack() instanceof Track track) {
                        String isrc = (track.getExternalIds() != null && track.getExternalIds().getExternalIds() != null)
                                ? track.getExternalIds().getExternalIds().get("isrc")
                                : null;

                        String imageUrl = (track.getAlbum() != null && track.getAlbum().getImages() != null && track.getAlbum().getImages().length > 0)
                                ? track.getAlbum().getImages()[0].getUrl()
                                : null;

                        List<String> artists = Arrays.stream(track.getArtists())
                                .map(a -> a.getName())
                                .collect(Collectors.toList());

                        String mainArtist = artists.isEmpty() ? "Unknown Artist" : artists.get(0);

                        trackList.add(TrackDto.builder()
                                .id(track.getId())
                                .title(track.getName())
                                .mainArtist(mainArtist)
                                .artists(artists)
                                .album(track.getAlbum() != null ? track.getAlbum().getName() : "")
                                .durationMs(Long.valueOf(track.getDurationMs()))
                                .isrc(isrc)
                                .imageUrl(imageUrl)
                                .spotifyUri(track.getUri())
                                .previewUrl(track.getPreviewUrl())
                                .status("PENDING")
                                .build());
                    }
                }

                offset += limit;
                hasMore = playlistTrackPaging.getNext() != null && trackList.size() < playlistTrackPaging.getTotal();
            }

            return trackList;
        } catch (Exception e) {
            log.warn("Error fetching tracks for Spotify playlist {}, using demo tracks: {}", playlistId, e.getMessage());
            return getDemoPlaylistTracks(playlistId);
        }
    }

    public List<PlaylistDto> getDemoPlaylists() {
        return List.of(
                PlaylistDto.builder()
                        .id("demo_playlist_synthwave")
                        .title("Synthwave & Cyberpunk Vibes")
                        .description("Neon lights, retro synthesizers, and midnight cruising beats.")
                        .owner("Alex Rivers")
                        .totalTracks(8)
                        .imageUrl("https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400&auto=format&fit=crop&q=80")
                        .uri("spotify:playlist:demo_playlist_synthwave")
                        .isPublic(true)
                        .collaborative(false)
                        .build(),
                PlaylistDto.builder()
                        .id("demo_playlist_lofi")
                        .title("Lo-Fi Chill & Study Beats")
                        .description("Soft vinyl textures, mellow chords, and focus rhythms for deep work.")
                        .owner("Alex Rivers")
                        .totalTracks(6)
                        .imageUrl("https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400&auto=format&fit=crop&q=80")
                        .uri("spotify:playlist:demo_playlist_lofi")
                        .isPublic(true)
                        .collaborative(false)
                        .build(),
                PlaylistDto.builder()
                        .id("demo_playlist_indie")
                        .title("Indie Pop & Dreamy Melodies")
                        .description("Sun-drenched guitars, dreamy vocals, and bedroom pop perfection.")
                        .owner("Alex Rivers")
                        .totalTracks(6)
                        .imageUrl("https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&auto=format&fit=crop&q=80")
                        .uri("spotify:playlist:demo_playlist_indie")
                        .isPublic(false)
                        .collaborative(false)
                        .build(),
                PlaylistDto.builder()
                        .id("demo_playlist_edm")
                        .title("Festival Anthems & High-Energy EDM")
                        .description("Massive drops, driving basslines, and mainstage club energy.")
                        .owner("Alex Rivers")
                        .totalTracks(6)
                        .imageUrl("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&auto=format&fit=crop&q=80")
                        .uri("spotify:playlist:demo_playlist_edm")
                        .isPublic(true)
                        .collaborative(false)
                        .build()
        );
    }

    private List<TrackDto> getDemoPlaylistTracks(String playlistId) {
        if (playlistId != null && playlistId.contains("lofi")) {
            return List.of(
                    createDemoTrack("lofi_1", "Still Breathing", "Kupla", List.of("Kupla"), "Life Forms", 174000L, "GBK3W2000001", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=200&auto=format&fit=crop&q=80", "yO1rO6p-UBo", "Kupla - Still Breathing (Official Audio)"),
                    createDemoTrack("lofi_2", "Nagashi", "Idealism", List.of("Idealism"), "Hiraeth", 148000L, "GBK3W2000002", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=200&auto=format&fit=crop&q=80", "4_hI6P9s174", "idealism - nagashi"),
                    createDemoTrack("lofi_3", "Affection", "Jinsang", List.of("Jinsang"), "Life", 123000L, "GBK3W2000003", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=200&auto=format&fit=crop&q=80", "1WqC1zO5Q30", "jinsang - affection"),
                    createDemoTrack("lofi_4", "[im closing my eyes]", "potsu", List.of("potsu", "shiloh dynasty"), "im closing my eyes", 132000L, "GBK3W2000004", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=200&auto=format&fit=crop&q=80", "t_x_O3oGZt4", "potsu - [im closing my eyes] (ft. shiloh dynasty)"),
                    createDemoTrack("lofi_5", "Far Away", "Tomppabeats", List.of("Tomppabeats"), "Harbor LP", 112000L, "GBK3W2000005", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=200&auto=format&fit=crop&q=80", "0a7m-Y4zUbg", "tomppabeats - far away"),
                    createDemoTrack("lofi_6", "In Your Arms", "Saib", List.of("Saib"), "Around the World", 160000L, "GBK3W2000006", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=200&auto=format&fit=crop&q=80", "W1o4jKzL5h0", "saib - In Your Arms")
            );
        } else if (playlistId != null && playlistId.contains("indie")) {
            return List.of(
                    createDemoTrack("indie_1", "Space Song", "Beach House", List.of("Beach House"), "Depression Cherry", 320000L, "USSUB1570104", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&auto=format&fit=crop&q=80", "RBtlPT23PTM", "Beach House - Space Song"),
                    createDemoTrack("indie_2", "Show Me How", "Men I Trust", List.of("Men I Trust"), "Oncle Jazz", 215000L, "CAK531800001", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&auto=format&fit=crop&q=80", "OZRYzH0Sp54", "Men I Trust - Show Me How"),
                    createDemoTrack("indie_3", "Sofia", "Clairo", List.of("Clairo"), "Immunity", 188000L, "USUM71911422", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&auto=format&fit=crop&q=80", "L9l8zCOwEII", "Clairo - Sofia (Official Audio)"),
                    createDemoTrack("indie_4", "Are You Bored Yet?", "Wallows", List.of("Wallows", "Clairo"), "Nothing Happens", 178000L, "USAT21900130", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&auto=format&fit=crop&q=80", "W3q8Od5qJio", "Wallows - Are You Bored Yet? (feat. Clairo)"),
                    createDemoTrack("indie_5", "Chamber of Reflection", "Mac DeMarco", List.of("Mac DeMarco"), "Salad Days", 231000L, "US2S71415010", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&auto=format&fit=crop&q=80", "p7Yp_P8Y6oA", "Mac DeMarco // Chamber of Reflection"),
                    createDemoTrack("indie_6", "Goodie Bag", "Still Woozy", List.of("Still Woozy"), "Lover's Entourage", 154000L, "QM6MZ1773030", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&auto=format&fit=crop&q=80", "uH9oT3G2f1M", "Still Woozy - Goodie Bag")
            );
        } else if (playlistId != null && playlistId.contains("edm")) {
            return List.of(
                    createDemoTrack("edm_1", "Levels", "Avicii", List.of("Avicii"), "Levels", 199000L, "SEUM71101234", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&auto=format&fit=crop&q=80", "_ovdm2yX4MA", "Avicii - Levels (Original Mix)"),
                    createDemoTrack("edm_2", "Animals", "Martin Garrix", List.of("Martin Garrix"), "Gold Skies EP", 176000L, "NLZ541300001", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&auto=format&fit=crop&q=80", "gCYcHz2167o", "Martin Garrix - Animals (Official Video)"),
                    createDemoTrack("edm_3", "Don't You Worry Child", "Swedish House Mafia", List.of("Swedish House Mafia", "John Martin"), "Until Now", 212000L, "GBAYE1200870", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&auto=format&fit=crop&q=80", "1y6smkh6c-0", "Swedish House Mafia - Don't You Worry Child ft. John Martin"),
                    createDemoTrack("edm_4", "Losing It", "FISHER", List.of("FISHER"), "Losing It", 248000L, "AUM121800001", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&auto=format&fit=crop&q=80", "o4yJ_q3L7k8", "FISHER - Losing It (Official Audio)"),
                    createDemoTrack("edm_5", "(It Goes Like) Nanana", "Peggy Gou", List.of("Peggy Gou"), "I Hear You", 231000L, "GBBKS2300050", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&auto=format&fit=crop&q=80", "8kK1i68G_00", "Peggy Gou - (It Goes Like) Nanana - Official Lyric Video"),
                    createDemoTrack("edm_6", "One More Time", "Daft Punk", List.of("Daft Punk"), "Discovery", 320000L, "FRZ030000010", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&auto=format&fit=crop&q=80", "FGBhQbmMxpc", "Daft Punk - One More Time (Official Video)")
            );
        } else {
            // Default: Synthwave Playlist
            return List.of(
                    createDemoTrack("synth_1", "Nightcall", "Kavinsky", List.of("Kavinsky", "Lovefoxxx"), "OutRun", 259000L, "FRUM71000320", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=200&auto=format&fit=crop&q=80", "MV_3Dpw-BRY", "Kavinsky - Nightcall (Drive Original Soundtrack)"),
                    createDemoTrack("synth_2", "Sunset", "The Midnight", List.of("The Midnight"), "Endless Summer", 326000L, "QM6MZ1600120", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=200&auto=format&fit=crop&q=80", "rDBbaGCCIhk", "The Midnight - Sunset [Official Audio]"),
                    createDemoTrack("synth_3", "Tech Noir", "GUNSHIP", List.of("GUNSHIP", "John Carpenter"), "GUNSHIP", 297000L, "GBKPL1500001", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=200&auto=format&fit=crop&q=80", "-EDk1c_z1vM", "GUNSHIP - Tech Noir [Official Music Video]"),
                    createDemoTrack("synth_4", "Running in the Night", "FM-84", List.of("FM-84", "Ollie Wride"), "Atlas", 270000L, "QM42K1600002", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=200&auto=format&fit=crop&q=80", "nLzmm_3zZzU", "FM-84 - Running in the Night (feat. Ollie Wride)"),
                    createDemoTrack("synth_5", "Resonance", "HOME", List.of("HOME"), "Odyssey", 212000L, "US7VG1400001", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=200&auto=format&fit=crop&q=80", "8GW6sLrK40k", "HOME - Resonance"),
                    createDemoTrack("synth_6", "Turbo Killer", "Carpenter Brut", List.of("Carpenter Brut"), "Trilogy", 208000L, "FR23R1500010", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=200&auto=format&fit=crop&q=80", "er416XiUp4g", "Carpenter Brut - Turbo Killer"),
                    createDemoTrack("synth_7", "Lovers", "Timecop1983", List.of("Timecop1983", "SEAWAVES"), "Night Drive", 255000L, "NLB951800005", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=200&auto=format&fit=crop&q=80", "P1g84fQZ90o", "Timecop1983 - Lovers (feat. SEAWAVES)"),
                    createDemoTrack("synth_8", "Star Eater", "Daniel Deluxe", List.of("Daniel Deluxe"), "Corruptor", 274000L, "USK891600003", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=200&auto=format&fit=crop&q=80", "l6FjQc4x30A", "Daniel Deluxe - Star Eater")
            );
        }
    }

    private TrackDto createDemoTrack(String id, String title, String artist, List<String> artists, String album, Long durationMs, String isrc, String img, String mockYtId, String mockYtTitle) {
        return TrackDto.builder()
                .id(id)
                .title(title)
                .mainArtist(artist)
                .artists(artists)
                .album(album)
                .durationMs(durationMs)
                .isrc(isrc)
                .imageUrl(img)
                .spotifyUri("spotify:track:" + id)
                .matchedYoutubeVideoId(mockYtId)
                .matchedYoutubeTitle(mockYtTitle)
                .matchConfidence(0.98)
                .status("PENDING")
                .build();
    }
}
