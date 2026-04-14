package com.example.sbtemplate.mono.application.users.ports.in;

import java.util.UUID;
import com.example.sbtemplate.mono.domain.model.User;
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
