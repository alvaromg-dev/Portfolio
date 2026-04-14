package com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.in.delete;

import java.util.UUID;

public record DeleteUserRequest(
    UUID userId
) {}
