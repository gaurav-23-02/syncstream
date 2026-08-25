package com.syncstream.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthStatusDto {
    private boolean spotifyConnected;
    private boolean googleConnected;
    private UserProfileDto spotifyUser;
    private UserProfileDto googleUser;
    private boolean demoMode;
    private boolean fullyAuthenticated;
}
