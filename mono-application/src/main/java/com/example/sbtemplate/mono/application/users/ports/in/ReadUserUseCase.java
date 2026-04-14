package com.example.sbtemplate.mono.application.users.ports.in;

import com.example.sbtemplate.mono.domain.model.User;
import lombok.Builder;
import java.util.UUID;

public interface ReadUserUseCase {

    User readUser(ReadUserCommand command);

    @Builder
    record ReadUserCommand(
        UUID clientId,
        UUID userId
    ) {}
}
