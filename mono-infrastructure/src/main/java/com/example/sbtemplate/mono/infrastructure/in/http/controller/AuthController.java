package com.example.sbtemplate.mono.infrastructure.in.http.controller;

import com.example.sbtemplate.mono.infrastructure.in.http.constants.EndpointsConstants;
import com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.in.auth.InvalidateTokenRequest;
import com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.in.auth.LoginRequest;
import com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.in.auth.RefreshTokenRequest;
import com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.in.auth.ValidateTokenRequest;
import com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.out.TokenResponse;
import com.example.sbtemplate.mono.infrastructure.in.http.security.TokenPair;
import com.example.sbtemplate.mono.infrastructure.in.http.security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@Slf4j
public class AuthController {

    @Autowired private TokenService tokenService;

    /**
     * Email Login Endpoint
     * returns access and refresh tokens
     * 200 OK
     * 401 UNAUTHORIZED - if credentials are invalid
     * @param request
     * @return
     */
    @PostMapping(EndpointsConstants.AUTH_EMAIL)
    public TokenResponse emailLogin(
        @RequestBody LoginRequest request
    ) {
        log.info(EndpointsConstants.AUTH_EMAIL);
        return tokenService.emailLogin(request);
    }

    /**
     * Email Refresh Endpoint
     * returns new access and refresh tokens
     * 200 OK
     * 400 BAD REQUEST - if refresh token is invalid or expired
     * @param request
     * @return
     */
    @PostMapping(EndpointsConstants.AUTH_REFRESH)
    public TokenResponse emailRefresh(
        @RequestBody RefreshTokenRequest request
    ) {
        log.info(EndpointsConstants.AUTH_REFRESH);
        TokenPair pair = tokenService.refresh(request.refreshToken());
        return TokenResponse.from(pair);
    }

    /**
     * Invalidate Token Endpoint
     * returns true if token was invalidated, false otherwise
     * 200 OK
     * @param request
     * @param authorization
     * @return
     */
    @PostMapping(EndpointsConstants.AUTH_INVALIDATE)
    public Boolean invalidate(
        @RequestBody InvalidateTokenRequest request,
        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        log.info(EndpointsConstants.AUTH_INVALIDATE);
        String token = request != null ? request.token() : null;
        return tokenService.invalidate(token, authorization);
    }

    /**
     * Validate Token Endpoint
     * returns true if token is valid, false otherwise
     * 200 OK
     * @param request
     * @param authorization
     * @return
     */
    @PostMapping(EndpointsConstants.AUTH_VALIDATE)
    public Boolean validate(
        @RequestBody(required = false) ValidateTokenRequest request,
        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        log.info(EndpointsConstants.AUTH_VALIDATE);
        String token = request != null ? request.token() : null;
        return tokenService.validate(token, authorization);
    }
}
