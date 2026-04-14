package com.alvaromg.portfolio.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alvaromg.portfolio.application.users.exceptions.UserNotFoundException;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserRolesUseCase;
import com.alvaromg.portfolio.application.users.ports.out.UserRepository;
import com.alvaromg.portfolio.domain.model.User;

@Service
@Transactional
public class UpdateUserRolesService implements UpdateUserRolesUseCase {

    @Autowired private UserRepository userRepository;

    @Override
    public User updateUserRoles(UpdateUserRolesCommand command) {
        return userRepository.updateRoles(command.id(), command.roles())
            .orElseThrow(() -> new UserNotFoundException(command.id()));
    }
}
