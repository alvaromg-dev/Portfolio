package com.alvaromg.portfolio.infrastructure.in.http.controller.dto.in.create;

public record CreateUserRequest(
    String givenNames,
    String familyNames,
    String email,
    String password
) {}