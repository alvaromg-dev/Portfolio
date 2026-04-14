package com.alvaromg.portfolio.application.users.ports.in;

import lombok.Builder;
import java.util.UUID;

public interface UpdateUserPasswordUseCase {

    Boolean updateUserPassword(UpdateUserPasswordCommand command);

    @Builder
    record UpdateUserPasswordCommand(
        UUID clientId,
        UUID userId,
        String oldPassword,
        String newPassword
    ) {}
}
