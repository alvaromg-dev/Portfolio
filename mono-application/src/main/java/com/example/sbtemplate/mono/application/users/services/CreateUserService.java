package com.example.sbtemplate.mono.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sbtemplate.mono.application.shared.exceptions.specific.InvalidArgumentException;
import com.example.sbtemplate.mono.application.users.constants.UserConstants;
import com.example.sbtemplate.mono.application.users.exceptions.UserNotFoundException;
import com.example.sbtemplate.mono.application.users.ports.in.AddDefaultRoleUseCase;
import com.example.sbtemplate.mono.application.users.ports.in.CreateUserUseCase;
import com.example.sbtemplate.mono.application.users.ports.in.AddDefaultRoleUseCase.AddDefaultRoleCommand;
import com.example.sbtemplate.mono.application.users.ports.out.UserRepository;
import com.example.sbtemplate.mono.domain.model.User;

@Service
@Transactional
public class CreateUserService implements CreateUserUseCase {

    @Autowired private AddDefaultRoleUseCase addDefaultRoleUseCase;
    @Autowired private UserRepository userRepository;

    @Override
    public User createUser(CreateUserCommand command) {

        if (userRepository.existsByEmail(command.email()))
            throw new InvalidArgumentException("Email already in use: " + command.email());

        if (!UserConstants.COMPILED_EMAIL_PATTERN.matcher(command.email()).matches())
            throw new InvalidArgumentException("Invalid email format: " + command.email());

        if (!UserConstants.COMPILED_PASSWORD_PATTERN.matcher(command.password()).matches())
            throw new InvalidArgumentException("Invalid password format");

        if (!UserConstants.COMPILED_NAME_PATTERN.matcher(command.givenNames().trim()).matches())
            throw new InvalidArgumentException("Invalid given names format: " + command.givenNames());

        User user = User.builder()
            .givenNames(command.givenNames().trim())
            .familyNames(command.familyNames().trim())
            .email(command.email().trim().toLowerCase())
            .password(command.password())
            .build();

        User createdUser = userRepository.create(user);
        addDefaultRoleUseCase.addDefaultRole(new AddDefaultRoleCommand(createdUser.getId()));

        return userRepository.findById(createdUser.getId())
            .orElseThrow(() -> new UserNotFoundException(createdUser.getId()));
    }
}
