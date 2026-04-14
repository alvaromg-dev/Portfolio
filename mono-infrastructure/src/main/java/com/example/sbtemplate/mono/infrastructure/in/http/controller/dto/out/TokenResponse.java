package com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.out;

import java.time.Instant;

import com.example.sbtemplate.mono.infrastructure.in.http.security.TokenPair;

public record TokenResponse(
    String tokenType,
    String accessToken,
    Instant accessExpiresAt,
    String refreshToken,
    Instant refreshExpiresAt
) {
    public static TokenResponse from(TokenPair pair) {
        return new TokenResponse(
            "Bearer",
            pair.accessToken(),
            pair.accessExpiresAt(),
            pair.refreshToken(),
            pair.refreshExpiresAt()
        );
    }
}
