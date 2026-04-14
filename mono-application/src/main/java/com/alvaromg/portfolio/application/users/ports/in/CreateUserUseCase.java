package com.alvaromg.portfolio.application.users.ports.in;

import com.alvaromg.portfolio.domain.model.User;
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
