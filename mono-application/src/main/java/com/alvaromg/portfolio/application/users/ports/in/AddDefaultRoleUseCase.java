package com.alvaromg.portfolio.application.users.ports.in;

import java.util.Objects;
import java.util.UUID;
import com.alvaromg.portfolio.domain.model.User;

public interface AddDefaultRoleUseCase {

    User addDefaultRole(AddDefaultRoleCommand command);

    record AddDefaultRoleCommand(UUID userId) {
        public AddDefaultRoleCommand {
            Objects.requireNonNull(userId, "User id is required");
        }
    }
}
