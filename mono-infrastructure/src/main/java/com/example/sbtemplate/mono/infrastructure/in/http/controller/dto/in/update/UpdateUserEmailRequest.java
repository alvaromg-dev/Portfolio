package com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.in.update;

import java.util.UUID;

public record UpdateUserEmailRequest(
    UUID userId,
    String email
) {}
