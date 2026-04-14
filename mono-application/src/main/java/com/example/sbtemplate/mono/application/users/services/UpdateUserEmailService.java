package com.example.sbtemplate.mono.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sbtemplate.mono.application.shared.exceptions.specific.InvalidArgumentException;
import com.example.sbtemplate.mono.application.users.exceptions.UserNotFoundException;
import com.example.sbtemplate.mono.application.users.ports.in.UpdateUserEmailUseCase;
import com.example.sbtemplate.mono.application.users.ports.out.UserRepository;
import com.example.sbtemplate.mono.domain.model.User;

@Service
@Transactional
public class UpdateUserEmailService implements UpdateUserEmailUseCase {

    @Autowired private UserRepository userRepository;

    @Override
    public User updateUserEmail(UpdateUserEmailCommand command) {
        String email = command.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new InvalidArgumentException("Email already in use: " + email);
        }
        return userRepository.updateEmail(command.userId(), email)
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
    }
}
