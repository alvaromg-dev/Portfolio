package com.alvaromg.portfolio.application.users.ports.in;

import java.util.UUID;
import com.alvaromg.portfolio.domain.model.User;
import lombok.Builder;

public interface UpdateUserEmailUseCase {

    User updateUserEmail(UpdateUserEmailCommand command);

    @Builder
    record UpdateUserEmailCommand(
        UUID userId,
        UUID clientId,
        String email
    ) {}
}
