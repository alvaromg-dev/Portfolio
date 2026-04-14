package com.alvaromg.portfolio.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alvaromg.portfolio.application.shared.exceptions.specific.InvalidArgumentException;
import com.alvaromg.portfolio.application.users.exceptions.UserNotFoundException;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserEmailUseCase;
import com.alvaromg.portfolio.application.users.ports.out.UserRepository;
import com.alvaromg.portfolio.domain.model.User;

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
