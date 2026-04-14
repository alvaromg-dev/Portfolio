package com.example.sbtemplate.mono.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sbtemplate.mono.application.users.exceptions.UserNotFoundException;
import com.example.sbtemplate.mono.application.users.ports.in.ReadUserUseCase;
import com.example.sbtemplate.mono.application.users.ports.out.UserRepository;
import com.example.sbtemplate.mono.domain.model.User;

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
