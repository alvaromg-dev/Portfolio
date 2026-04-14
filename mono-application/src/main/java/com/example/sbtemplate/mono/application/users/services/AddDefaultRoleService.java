package com.example.sbtemplate.mono.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sbtemplate.mono.application.roles.constants.RolesConstants;
import com.example.sbtemplate.mono.application.roles.ports.in.ReadRoleByCodeUseCase;
import com.example.sbtemplate.mono.application.roles.ports.in.ReadRoleByCodeUseCase.ReadRoleByCodeCommand;
import com.example.sbtemplate.mono.application.users.exceptions.UserNotFoundException;
import com.example.sbtemplate.mono.application.users.ports.in.AddDefaultRoleUseCase;
import com.example.sbtemplate.mono.application.users.ports.out.UserRepository;
import com.example.sbtemplate.mono.domain.model.Role;
import com.example.sbtemplate.mono.domain.model.User;

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
