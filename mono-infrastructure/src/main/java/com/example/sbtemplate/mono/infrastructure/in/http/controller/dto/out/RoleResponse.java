package com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.out;

import java.util.UUID;
import lombok.Builder;

@Builder
public record RoleResponse(
    UUID id,
    String code,
    String description
) {}
