package com.example.sbtemplate.mono.application.users.ports.in;

import java.util.Objects;
import java.util.UUID;
import com.example.sbtemplate.mono.domain.model.User;

public interface AddDefaultRoleUseCase {

    User addDefaultRole(AddDefaultRoleCommand command);

    record AddDefaultRoleCommand(UUID userId) {
        public AddDefaultRoleCommand {
            Objects.requireNonNull(userId, "User id is required");
        }
    }
}
