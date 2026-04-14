package com.example.sbtemplate.mono.infrastructure.in.http.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String DEFAULT_SECRET = "change-me-please-change-me-please-32-bytes";
    private static final int MIN_KEY_BYTES = 32;

    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final SecretKey secretKey;
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public JwtService(
        @Value("${security.jwt.secret:" + DEFAULT_SECRET + "}") String secret,
        @Value("${security.jwt.access-ttl:PT30M}") Duration accessTtl,
        @Value("${security.jwt.refresh-ttl:P7D}") Duration refreshTtl
    ) {
        this.secretKey = buildKey(secret);
        this.encoder = NimbusJwtEncoder.withSecretKey(secretKey).algorithm(MacAlgorithm.HS256).build();
        this.decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    public Jwt createToken(AuthenticatedUser user, TokenType type) {
        Instant now = Instant.now();
        Duration ttl = ttlFor(type);
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(now.plus(ttl))
            .id(UUID.randomUUID().toString())
            .claim("roles", authorities(user))
            .claim("type", type.name())
            .build();
        return encoder.encode(JwtEncoderParameters.from(header, claims));
    }

    public Optional<Jwt> decode(String token) {
        try {
            return Optional.of(decoder.decode(token));
        } catch (JwtException ex) {
            return Optional.empty();
        }
    }

    private Duration ttlFor(TokenType type) {
        return switch (type) {
            case ACCESS -> accessTtl;
            case REFRESH -> refreshTtl;
        };
    }

    private static Set<String> authorities(AuthenticatedUser user) {
        return user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toUnmodifiableSet());
    }

    private static SecretKey buildKey(String rawSecret) {
        String effective = (rawSecret == null || rawSecret.isBlank()) ? DEFAULT_SECRET : rawSecret;
        byte[] keyBytes = effective.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_BYTES) {
            keyBytes = sha256(keyBytes);
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
