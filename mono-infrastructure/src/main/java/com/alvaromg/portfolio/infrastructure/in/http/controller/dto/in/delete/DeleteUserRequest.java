package com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.delete;

import java.util.UUID;

public record DeleteUserRequest(
    UUID userId
) {}
