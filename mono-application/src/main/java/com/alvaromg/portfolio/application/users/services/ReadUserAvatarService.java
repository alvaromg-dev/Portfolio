package com.alvaromg.portfolio.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alvaromg.portfolio.application.users.exceptions.UserNotFoundException;
import com.alvaromg.portfolio.application.users.ports.in.ReadUserAvatarUseCase;
import com.alvaromg.portfolio.application.users.ports.out.UserRepository;

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
