package com.example.sbtemplate.mono.application.roles.ports.out;

import java.util.Optional;
import com.example.sbtemplate.mono.domain.model.Role;

public interface RoleRepository {

    // READ
    Optional<Role> findByCode(String code);

    // CREATE

    // UPDATE

    // DELETE
}
