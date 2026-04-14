package com.alvaromg.portfolio.application.users.ports.in;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import com.alvaromg.portfolio.application.roles.constants.RolesConstants;
import com.alvaromg.portfolio.domain.model.Role;
import com.alvaromg.portfolio.domain.model.User;

public interface UpdateUserRolesUseCase {

    User updateUserRoles(UpdateUserRolesCommand command);

    record UpdateUserRolesCommand(
        UUID id,
        Set<Role> roles
    ) {
        public UpdateUserRolesCommand {
            Objects.requireNonNull(id, "User id is required");
            validateRoles(roles);
        }

        private void validateRoles(Set<Role> roles) {
            Set<String> roleCodes = new LinkedHashSet<>();
            for (Role role : roles) {
                roleCodes.add(role.getCode());
            }
            if (!roleCodes.containsAll(RolesConstants.REQUIRED)) {
                throw new IllegalArgumentException(
                        "User roles must contain at least the required roles: " + RolesConstants.REQUIRED);
            }
        }
    }
}
