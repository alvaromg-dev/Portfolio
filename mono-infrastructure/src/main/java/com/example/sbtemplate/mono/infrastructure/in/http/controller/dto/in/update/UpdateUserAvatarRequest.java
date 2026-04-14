package com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.in.update;

import java.util.UUID;

public record UpdateUserAvatarRequest(
    UUID userId,
    String avatar
) {}
