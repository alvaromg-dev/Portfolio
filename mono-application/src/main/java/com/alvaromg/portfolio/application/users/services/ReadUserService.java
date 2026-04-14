package com.alvaromg.portfolio.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alvaromg.portfolio.application.users.exceptions.UserNotFoundException;
import com.alvaromg.portfolio.application.users.ports.in.ReadUserUseCase;
import com.alvaromg.portfolio.application.users.ports.out.UserRepository;
import com.alvaromg.portfolio.domain.model.User;

@Service
@Transactional(readOnly = true)
public class ReadUserService implements ReadUserUseCase {

    @Autowired private UserRepository userRepository;

    @Override
    public User readUser(ReadUserCommand command) {
        return userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
    }
}
