package com.alvaromg.portfolio.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alvaromg.portfolio.application.roles.constants.RolesConstants;
import com.alvaromg.portfolio.application.roles.ports.in.ReadRoleByCodeUseCase;
import com.alvaromg.portfolio.application.roles.ports.in.ReadRoleByCodeUseCase.ReadRoleByCodeCommand;
import com.alvaromg.portfolio.application.users.exceptions.UserNotFoundException;
import com.alvaromg.portfolio.application.users.ports.in.AddDefaultRoleUseCase;
import com.alvaromg.portfolio.application.users.ports.out.UserRepository;
import com.alvaromg.portfolio.domain.model.Role;
import com.alvaromg.portfolio.domain.model.User;

@Service
@Transactional
public class AddDefaultRoleService implements AddDefaultRoleUseCase {

    @Autowired private ReadRoleByCodeUseCase readRoleByCodeUseCase;
    @Autowired private UserRepository userRepository;

    @Override
    public User addDefaultRole(AddDefaultRoleCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        for (String roleCode : RolesConstants.REQUIRED) {
            Role role = readRoleByCodeUseCase.getRoleByCode(new ReadRoleByCodeCommand(roleCode));
            if (!user.getRoles().contains(role)) {
                user.getRoles().add(role);
            }
        }

        return userRepository.updateRoles(user.getId(), user.getRoles())
            .orElseThrow(() -> new UserNotFoundException(user.getId()));
    }
}
