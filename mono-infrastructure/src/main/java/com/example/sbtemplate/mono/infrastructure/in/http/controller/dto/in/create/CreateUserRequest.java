package com.example.sbtemplate.mono.infrastructure.in.http.controller.dto.in.create;

public record CreateUserRequest(
    String givenNames,
    String familyNames,
    String email,
    String password
) {}