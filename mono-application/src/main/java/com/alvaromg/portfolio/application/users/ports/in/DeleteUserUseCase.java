package com.alvaromg.portfolio.application.users.ports.in;

import java.util.UUID;
import lombok.Builder;

public interface DeleteUserUseCase {

    boolean delete(DeleteUserCommand command);

    @Builder
    record DeleteUserCommand(
        UUID clientId,
        UUID userId
    ) {}
}
