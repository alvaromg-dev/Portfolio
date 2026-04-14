package com.alvaromg.portfolio.application.users.ports.in;

import java.util.UUID;
import com.alvaromg.portfolio.domain.model.User;
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
