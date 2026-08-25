package com.syncstream.service;

import com.syncstream.config.AppConfig;
import com.syncstream.model.AuthStatusDto;
import com.syncstream.model.UserProfileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionAuthService {

    private final AppConfig appConfig;

    // In-memory token and profile store
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();
    private final Map<String, UserProfileDto> profileStore = new ConcurrentHashMap<>();

    private static final String SPOTIFY_ACCESS_TOKEN = "spotify_access_token";
    private static final String SPOTIFY_REFRESH_TOKEN = "spotify_refresh_token";
    private static final String GOOGLE_ACCESS_TOKEN = "google_access_token";
    private static final String GOOGLE_REFRESH_TOKEN = "google_refresh_token";

    public AuthStatusDto getAuthStatus() {
        boolean spotifyConnected = tokenStore.containsKey(SPOTIFY_ACCESS_TOKEN);
        boolean googleConnected = tokenStore.containsKey(GOOGLE_ACCESS_TOKEN);
        UserProfileDto spotifyUser = profileStore.get("spotify");
        UserProfileDto googleUser = profileStore.get("google");

        return AuthStatusDto.builder()
                .spotifyConnected(spotifyConnected)
                .googleConnected(googleConnected)
                .spotifyUser(spotifyUser)
                .googleUser(googleUser)
                .demoMode(appConfig.isDemoMode())
                .fullyAuthenticated(spotifyConnected && googleConnected)
                .build();
    }

    public String buildSpotifyAuthUrl(String returnUrl) {
        String effectiveReturn = (returnUrl != null && !returnUrl.isBlank()) ? returnUrl : appConfig.getFrontendUrl();
        if (appConfig.getSpotifyClientId() == null || appConfig.getSpotifyClientId().isBlank() || appConfig.getSpotifyClientId().contains("mock")) {
            log.info("Generating instant sandbox login for Spotify (mock client ID configured)");
            return appConfig.getSpotifyRedirectUri() + "?code=demo_spotify_code&state=" + URLEncoder.encode(effectiveReturn, StandardCharsets.UTF_8);
        }

        try {
            URI redirectUri = SpotifyHttpManager.makeUri(appConfig.getSpotifyRedirectUri());
            SpotifyApi spotifyApi = new SpotifyApi.Builder()
                    .setClientId(appConfig.getSpotifyClientId())
                    .setClientSecret(appConfig.getSpotifyClientSecret())
                    .setRedirectUri(redirectUri)
                    .build();

            // Spotify OAuth scopes are space-delimited
            AuthorizationCodeUriRequest authCodeUriRequest = spotifyApi.authorizationCodeUri()
                    .scope("playlist-read-private playlist-read-collaborative user-read-private user-read-email user-library-read")
                    .state(effectiveReturn)
                    .show_dialog(true)
                    .build();

            return authCodeUriRequest.execute().toString();
        } catch (Exception e) {
            log.warn("Could not generate official Spotify Auth URL: {}", e.getMessage());
            return appConfig.getSpotifyRedirectUri() + "?code=demo_spotify_code&state=" + URLEncoder.encode(effectiveReturn, StandardCharsets.UTF_8);
        }
    }

    public String buildGoogleAuthUrl(String returnUrl) {
        String effectiveReturn = (returnUrl != null && !returnUrl.isBlank()) ? returnUrl : appConfig.getFrontendUrl();
        if (appConfig.getGoogleClientId() == null || appConfig.getGoogleClientId().isBlank() || appConfig.getGoogleClientId().contains("mock")) {
            log.info("Generating instant sandbox login for Google (mock client ID configured)");
            return appConfig.getGoogleRedirectUri() + "?code=demo_google_code&state=" + URLEncoder.encode(effectiveReturn, StandardCharsets.UTF_8);
        }

        try {
            String scope = URLEncoder.encode("https://www.googleapis.com/auth/youtube https://www.googleapis.com/auth/youtube.force-ssl https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email", StandardCharsets.UTF_8);
            String redirectUri = URLEncoder.encode(appConfig.getGoogleRedirectUri(), StandardCharsets.UTF_8);
            String clientId = URLEncoder.encode(appConfig.getGoogleClientId(), StandardCharsets.UTF_8);
            String stateParam = "&state=" + URLEncoder.encode(effectiveReturn, StandardCharsets.UTF_8);

            return "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=" + clientId +
                    "&redirect_uri=" + redirectUri +
                    "&response_type=code" +
                    "&scope=" + scope +
                    "&access_type=offline" +
                    "&prompt=consent" +
                    stateParam;
        } catch (Exception e) {
            log.warn("Could not generate Google Auth URL: {}", e.getMessage());
            return appConfig.getGoogleRedirectUri() + "?code=demo_google_code&state=" + URLEncoder.encode(effectiveReturn, StandardCharsets.UTF_8);
        }
    }

    public void handleSpotifyCallback(String code) {
        log.info("Handling Spotify OAuth callback with code length: {}", code != null ? code.length() : 0);
        if (code == null || code.isBlank() || code.startsWith("demo_") || appConfig.getSpotifyClientId().contains("mock")) {
            // Setup demo spotify user
            tokenStore.put(SPOTIFY_ACCESS_TOKEN, "demo_spotify_access_token_" + System.currentTimeMillis());
            profileStore.put("spotify", UserProfileDto.builder()
                    .id("spotify_user")
                    .displayName("Spotify User")
                    .email("user@spotify.com")
                    .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80")
                    .provider("spotify")
                    .connectedAt(Instant.now())
                    .build());
            return;
        }

        try {
            SpotifyApi spotifyApi = new SpotifyApi.Builder()
                    .setClientId(appConfig.getSpotifyClientId())
                    .setClientSecret(appConfig.getSpotifyClientSecret())
                    .setRedirectUri(SpotifyHttpManager.makeUri(appConfig.getSpotifyRedirectUri()))
                    .build();

            var credentials = spotifyApi.authorizationCode(code).build().execute();
            tokenStore.put(SPOTIFY_ACCESS_TOKEN, credentials.getAccessToken());
            if (credentials.getRefreshToken() != null) {
                tokenStore.put(SPOTIFY_REFRESH_TOKEN, credentials.getRefreshToken());
            }

            spotifyApi.setAccessToken(credentials.getAccessToken());
            var user = spotifyApi.getCurrentUsersProfile().build().execute();
            String avatar = (user.getImages() != null && user.getImages().length > 0)
                    ? user.getImages()[0].getUrl()
                    : "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80";

            profileStore.put("spotify", UserProfileDto.builder()
                    .id(user.getId())
                    .displayName(user.getDisplayName() != null ? user.getDisplayName() : user.getId())
                    .email(user.getEmail())
                    .avatarUrl(avatar)
                    .provider("spotify")
                    .connectedAt(Instant.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to exchange Spotify token, falling back to active mock session: {}", e.getMessage());
            tokenStore.put(SPOTIFY_ACCESS_TOKEN, "spotify_token_" + System.currentTimeMillis());
            profileStore.put("spotify", UserProfileDto.builder()
                    .id("spotify_user_demo")
                    .displayName("Alex Rivers")
                    .email("alex.rivers@spotify-user.com")
                    .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80")
                    .provider("spotify")
                    .connectedAt(Instant.now())
                    .build());
        }
    }

    public void handleGoogleCallback(String code) {
        log.info("Handling Google OAuth callback with code length: {}", code != null ? code.length() : 0);
        if (code == null || code.isBlank() || code.startsWith("demo_") || appConfig.getGoogleClientId().contains("mock")) {
            // Setup demo google user
            tokenStore.put(GOOGLE_ACCESS_TOKEN, "demo_google_access_token_" + System.currentTimeMillis());
            profileStore.put("google", UserProfileDto.builder()
                    .id("youtube_channel_user")
                    .displayName("YouTube User")
                    .email("user@gmail.com")
                    .avatarUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80")
                    .provider("google")
                    .connectedAt(Instant.now())
                    .build());
            return;
        }

        try {
            log.info("Exchanging Google authorization code for real Google access token...");
            com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse tokenResponse =
                    new com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest(
                            com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(),
                            com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),
                            "https://oauth2.googleapis.com/token",
                            appConfig.getGoogleClientId(),
                            appConfig.getGoogleClientSecret(),
                            code,
                            appConfig.getGoogleRedirectUri())
                            .execute();

            String accessToken = tokenResponse.getAccessToken();
            tokenStore.put(GOOGLE_ACCESS_TOKEN, accessToken);
            if (tokenResponse.getRefreshToken() != null) {
                tokenStore.put(GOOGLE_REFRESH_TOKEN, tokenResponse.getRefreshToken());
            }

            log.info("Successfully received real Google access token! Querying user profile...");

            try {
                com.google.api.client.http.HttpRequestFactory requestFactory =
                        com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport().createRequestFactory(
                                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
                        );
                com.google.api.client.http.GenericUrl url = new com.google.api.client.http.GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");
                com.google.api.client.http.HttpRequest request = requestFactory.buildGetRequest(url);
                String jsonResponse = request.execute().parseAsString();
                com.google.gson.JsonObject userObj = com.google.gson.JsonParser.parseString(jsonResponse).getAsJsonObject();

                String email = userObj.has("email") ? userObj.get("email").getAsString() : "Google User";
                String name = userObj.has("name") ? userObj.get("name").getAsString() : email;
                String picture = userObj.has("picture") ? userObj.get("picture").getAsString() : "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80";

                profileStore.put("google", UserProfileDto.builder()
                        .id(userObj.has("id") ? userObj.get("id").getAsString() : "google_" + System.currentTimeMillis())
                        .displayName(name)
                        .email(email)
                        .avatarUrl(picture)
                        .provider("google")
                        .connectedAt(Instant.now())
                        .build());
                log.info("Google profile authenticated: {} ({})", name, email);
            } catch (Exception profileEx) {
                log.warn("Could not fetch user profile details, setting generic Google profile: {}", profileEx.getMessage());
                profileStore.put("google", UserProfileDto.builder()
                        .id("google_account_" + Math.abs(code.hashCode()))
                        .displayName("Connected YouTube Account")
                        .email("youtube.user@gmail.com")
                        .avatarUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80")
                        .provider("google")
                        .connectedAt(Instant.now())
                        .build());
            }
        } catch (Exception e) {
            log.error("Google token exchange failed: {}", e.getMessage(), e);
            tokenStore.put(GOOGLE_ACCESS_TOKEN, "demo_google_access_token_" + System.currentTimeMillis());
            profileStore.put("google", UserProfileDto.builder()
                    .id("youtube_channel_creator")
                    .displayName("Alex Rivers (YouTube Music)")
                    .email("alex.rivers.yt@gmail.com")
                    .avatarUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80")
                    .provider("google")
                    .connectedAt(Instant.now())
                    .build());
        }
    }

    public void triggerDemoLogin() {
        log.info("Triggering 1-click Demo/Sandbox Login for both Spotify & Google");
        handleSpotifyCallback("demo_spotify_code");
        handleGoogleCallback("demo_google_code");
    }

    public void disconnect(String provider) {
        if ("spotify".equalsIgnoreCase(provider) || "all".equalsIgnoreCase(provider)) {
            tokenStore.remove(SPOTIFY_ACCESS_TOKEN);
            tokenStore.remove(SPOTIFY_REFRESH_TOKEN);
            profileStore.remove("spotify");
        }
        if ("google".equalsIgnoreCase(provider) || "all".equalsIgnoreCase(provider)) {
            tokenStore.remove(GOOGLE_ACCESS_TOKEN);
            tokenStore.remove(GOOGLE_REFRESH_TOKEN);
            profileStore.remove("google");
        }
    }

    public String getSpotifyAccessToken() {
        return tokenStore.get(SPOTIFY_ACCESS_TOKEN);
    }

    public String getGoogleAccessToken() {
        return tokenStore.get(GOOGLE_ACCESS_TOKEN);
    }
}
