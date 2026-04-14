package com.alvaromg.portfolio.application.users.ports.in;

import com.alvaromg.portfolio.domain.model.User;
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
