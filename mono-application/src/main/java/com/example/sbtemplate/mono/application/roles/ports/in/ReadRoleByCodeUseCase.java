package com.example.sbtemplate.mono.application.roles.ports.in;

import com.example.sbtemplate.mono.domain.model.Role;
import java.util.Objects;

public interface ReadRoleByCodeUseCase {

    Role getRoleByCode(ReadRoleByCodeCommand command);

    record ReadRoleByCodeCommand(String code) {
        public ReadRoleByCodeCommand {
            Objects.requireNonNull(code, "Role code is required");
        }
    }
}
