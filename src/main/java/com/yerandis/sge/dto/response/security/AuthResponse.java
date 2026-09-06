package com.yerandis.sge.dto.response.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;    // "Bearer"
    private long   expiresIn;    // segundos
    private UserInfo user;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String username;
        private List<String> roles;
        private List<String> permissions;
        private UUID   employeeId;
    }
}