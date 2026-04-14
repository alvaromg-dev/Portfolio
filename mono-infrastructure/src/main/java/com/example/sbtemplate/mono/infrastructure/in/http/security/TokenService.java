package com.example.sbtemplate.mono.infrastructure.in.http.security;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.example.sbtemplate.mono.application.shared.exceptions.specific.InvalidArgumentException;
import com.example.sbtemplate.mono.application.shared.exceptions.specific.UnauthorizedException;
import com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.in.auth.LoginRequest;
import com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.out.TokenResponse;

@Service
public class TokenService {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService jwtService;
    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public TokenPair issueTokens(AuthenticatedUser user) {
        Objects.requireNonNull(user, "user");
        Jwt access = jwtService.createToken(user, TokenType.ACCESS);
        Jwt refresh = jwtService.createToken(user, TokenType.REFRESH);
        return new TokenPair(
            access.getTokenValue(),
            access.getExpiresAt(),
            refresh.getTokenValue(),
            refresh.getExpiresAt()
        );
    }

    public Optional<AuthenticatedUser> authenticate(String accessToken) {
        return jwtService.decode(accessToken)
            .filter(jwt -> TokenType.ACCESS.name().equals(jwt.getClaimAsString("type")))
            .filter(jwt -> !isRevoked(jwt))
            .map(this::buildPrincipal);
    }

    public TokenPair refresh(String refreshToken) {
        Jwt jwt = jwtService.decode(refreshToken)
            .filter(decoded -> TokenType.REFRESH.name().equals(decoded.getClaimAsString("type")))
            .orElseThrow(() -> new InvalidArgumentException("Refresh token not found"));

        if (isRevoked(jwt)) {
            throw new InvalidArgumentException("Refresh token invalidated");
        }

        revoke(jwt);
        AuthenticatedUser user = buildPrincipal(jwt);
        return issueTokens(user);
    }

    public Boolean invalidate(String token, String authorization) {
        if ((token == null || token.isBlank()) && authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring("Bearer ".length()).trim();
        }
        if (token == null || token.isBlank()) {
            throw new InvalidArgumentException("Token is required to invalidate a session");
        }
        Optional<Jwt> decoded = jwtService.decode(token);
        if (decoded.isEmpty()) {
            return false;
        }
        revoke(decoded.get());
        return true;
    }

    public Boolean validate(String token, String authorization) {
        if ((token == null || token.isBlank()) && authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring("Bearer ".length()).trim();
        }
        if (token == null || token.isBlank()) {
            throw new InvalidArgumentException("Token is required to validate");
        }
        return authenticate(token).isPresent();
    }

    private AuthenticatedUser buildPrincipal(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String username = jwt.getSubject();
        List<String> roles = jwt.getClaimAsStringList("roles");
        Set<SimpleGrantedAuthority> authorities = roles == null
            ? Set.of()
            : roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        return new AuthenticatedUser(userId, username, "N/A", authorities, true);
    }

    private boolean isRevoked(Jwt jwt) {
        String jti = jwt.getId();
        if (jti == null) {
            return false;
        }
        Instant exp = revokedTokens.get(jti);
        if (exp == null) {
            return false;
        }
        if (exp.isBefore(Instant.now())) {
            revokedTokens.remove(jti);
            return false;
        }
        return true;
    }

    private void revoke(Jwt jwt) {
        String jti = jwt.getId();
        Instant exp = jwt.getExpiresAt();
        if (jti != null && exp != null) {
            revokedTokens.put(jti, exp);
        }
    }

    public TokenResponse emailLogin(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
            TokenPair pair = this.issueTokens(principal);
            return TokenResponse.from(pair);
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }
}
