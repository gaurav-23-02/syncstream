package com.syncstream.controller;

import com.syncstream.config.AppConfig;
import com.syncstream.model.AuthStatusDto;
import com.syncstream.service.SessionAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SessionAuthService sessionAuthService;
    private final AppConfig appConfig;

    @GetMapping("/status")
    public ResponseEntity<AuthStatusDto> getStatus() {
        return ResponseEntity.ok(sessionAuthService.getAuthStatus());
    }

    @GetMapping("/spotify/login")
    public ResponseEntity<Map<String, String>> getSpotifyLoginUrl(
            @RequestParam(name = "redirect", required = false) String redirectUrl) {
        String url = sessionAuthService.buildSpotifyAuthUrl(redirectUrl);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/spotify/callback")
    public void spotifyCallback(@RequestParam(name = "code", required = false) String code,
                                @RequestParam(name = "state", required = false) String state,
                                @RequestParam(name = "error", required = false) String error,
                                HttpServletResponse response) throws IOException {
        log.info("Received Spotify OAuth callback, code: {}, error: {}, state: {}", code != null, error, state);
        if (code != null) {
            sessionAuthService.handleSpotifyCallback(code);
        }

        String targetDomain = (state != null && state.startsWith("http")) ? state : appConfig.getFrontendUrl();
        String separator = targetDomain.contains("?") ? "&" : "?";
        response.sendRedirect(targetDomain + separator + "auth=" + (error != null ? "spotify_error" : "spotify_success"));
    }

    @GetMapping("/google/login")
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl(
            @RequestParam(name = "redirect", required = false) String redirectUrl) {
        String url = sessionAuthService.buildGoogleAuthUrl(redirectUrl);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam(name = "code", required = false) String code,
                               @RequestParam(name = "state", required = false) String state,
                               @RequestParam(name = "error", required = false) String error,
                               HttpServletResponse response) throws IOException {
        log.info("Received Google OAuth callback, code: {}, error: {}, state: {}", code != null, error, state);
        if (code != null) {
            sessionAuthService.handleGoogleCallback(code);
        }

        String targetDomain = (state != null && state.startsWith("http")) ? state : appConfig.getFrontendUrl();
        String separator = targetDomain.contains("?") ? "&" : "?";
        response.sendRedirect(targetDomain + separator + "auth=" + (error != null ? "google_error" : "google_success"));
    }

    @PostMapping("/demo-login")
    public ResponseEntity<AuthStatusDto> demoLogin() {
        sessionAuthService.triggerDemoLogin();
        return ResponseEntity.ok(sessionAuthService.getAuthStatus());
    }

    @PostMapping("/disconnect/{provider}")
    public ResponseEntity<AuthStatusDto> disconnect(@PathVariable("provider") String provider) {
        sessionAuthService.disconnect(provider);
        return ResponseEntity.ok(sessionAuthService.getAuthStatus());
    }
}
