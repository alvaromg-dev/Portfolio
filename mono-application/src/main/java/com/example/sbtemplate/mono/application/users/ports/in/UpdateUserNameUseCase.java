package com.example.sbtemplate.mono.application.users.ports.in;

import java.util.UUID;
import com.example.sbtemplate.mono.domain.model.User;
import lombok.Builder;

public interface UpdateUserNameUseCase {

    User updateUserName(UpdateUserNameCommand command);

    @Builder
    record UpdateUserNameCommand(
        UUID userId,
        UUID clientId,
        String givenNames,
        String familyNames
    ) {}
}
