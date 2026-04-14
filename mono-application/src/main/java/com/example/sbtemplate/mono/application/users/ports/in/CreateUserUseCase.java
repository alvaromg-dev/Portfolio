package com.example.sbtemplate.mono.application.users.ports.in;

import com.example.sbtemplate.mono.domain.model.User;
import lombok.Builder;

public interface CreateUserUseCase {

    User createUser(CreateUserCommand command);

    @Builder
    record CreateUserCommand(
        String givenNames,
        String familyNames,
        String email,
        String password
    ) {}
}
