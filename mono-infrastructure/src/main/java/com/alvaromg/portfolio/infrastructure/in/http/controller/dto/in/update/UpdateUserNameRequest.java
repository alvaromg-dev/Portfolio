package com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.update;

import java.util.UUID;

public record UpdateUserNameRequest(
    UUID userId,
    String givenNames,
    String familyNames
) {}
