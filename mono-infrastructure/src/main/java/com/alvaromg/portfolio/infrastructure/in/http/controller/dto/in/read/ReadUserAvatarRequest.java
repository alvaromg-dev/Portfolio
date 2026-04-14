package com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.read;

import java.util.UUID;

public record ReadUserAvatarRequest(
    UUID userId
) {}
