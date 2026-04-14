package com.alvaromg.portfolio.application.roles.ports.out;

import java.util.Optional;
import com.alvaromg.portfolio.domain.model.Role;

public interface RoleRepository {

    // READ
    Optional<Role> findByCode(String code);

    // CREATE

    // UPDATE

    // DELETE
}
