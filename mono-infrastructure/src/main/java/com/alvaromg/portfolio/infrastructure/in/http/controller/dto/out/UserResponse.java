package com.alvaromg.portfolio.infrastructure.in.http.controller.dto.out;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserResponse(
    UUID id,
    String givenNames,
    String familyNames,
    String nif,
    String email,
    String phone,
    Set<RoleResponse> roles,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {}
