package com.syncstream.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackMatchingCacheService {

    private final Cache<String, String> trackMatchCache;

    private static final Pattern CLEAN_EXTRA_INFO = Pattern.compile(
            "\\s*\\((?:feat\\.?|ft\\.?|remastered|deluxe|version|radio edit|original mix|official audio|official video|mono|stereo|live|anniversary|expanded).*?\\)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CLEAN_FEAT_DASH = Pattern.compile(
            "\\s*-(?:\\s*remastered.*|\\s*radio edit.*|\\s*live.*)",
            Pattern.CASE_INSENSITIVE
    );

    public String normalizeKey(String artist, String title) {
        String safeArtist = (artist == null ? "" : artist).trim().toLowerCase(Locale.ROOT);
        String safeTitle = (title == null ? "" : title).trim().toLowerCase(Locale.ROOT);

        // Strip extraneous parenthesized metadata
        safeTitle = CLEAN_EXTRA_INFO.matcher(safeTitle).replaceAll("");
        safeTitle = CLEAN_FEAT_DASH.matcher(safeTitle).replaceAll("");
        safeTitle = safeTitle.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        safeArtist = safeArtist.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();

        return safeArtist + "::" + safeTitle;
    }

    public Optional<String> get(String artist, String title, String isrc) {
        if (isrc != null && !isrc.isBlank()) {
            String videoId = trackMatchCache.getIfPresent("ISRC::" + isrc.trim().toUpperCase(Locale.ROOT));
            if (videoId != null) {
                log.debug("Cache hit for ISRC: {} -> {}", isrc, videoId);
                return Optional.of(videoId);
            }
        }

        String key = normalizeKey(artist, title);
        String videoId = trackMatchCache.getIfPresent(key);
        if (videoId != null) {
            log.debug("Cache hit for Track: {} -> {}", key, videoId);
            return Optional.of(videoId);
        }

        return Optional.empty();
    }

    public void put(String artist, String title, String isrc, String videoId) {
        if (videoId == null || videoId.isBlank()) {
            return;
        }
        if (isrc != null && !isrc.isBlank()) {
            trackMatchCache.put("ISRC::" + isrc.trim().toUpperCase(Locale.ROOT), videoId);
        }
        String key = normalizeKey(artist, title);
        trackMatchCache.put(key, videoId);
        log.debug("Cached mapping for {} -> {}", key, videoId);
    }

    public long getCacheSize() {
        return trackMatchCache.estimatedSize();
    }

    public com.github.benmanes.caffeine.cache.stats.CacheStats getStats() {
        return trackMatchCache.stats();
    }
}
