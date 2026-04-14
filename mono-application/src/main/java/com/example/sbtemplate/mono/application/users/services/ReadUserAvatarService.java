package com.example.sbtemplate.mono.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sbtemplate.mono.application.users.exceptions.UserNotFoundException;
import com.example.sbtemplate.mono.application.users.ports.in.ReadUserAvatarUseCase;
import com.example.sbtemplate.mono.application.users.ports.out.UserRepository;

@Service
@Transactional(readOnly = true)
public class ReadUserAvatarService implements ReadUserAvatarUseCase {

    @Autowired private UserRepository userRepository;

    @Override
    public String readUserAvatar(ReadUserAvatarCommand command) {
        return userRepository.findAvatarById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
    }
}
