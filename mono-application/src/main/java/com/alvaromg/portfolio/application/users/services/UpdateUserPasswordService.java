package com.alvaromg.portfolio.application.users.services;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alvaromg.portfolio.application.users.ports.in.UpdateUserPasswordUseCase;
import com.alvaromg.portfolio.application.users.ports.out.UserRepository;

@Service
@Transactional
public class UpdateUserPasswordService implements UpdateUserPasswordUseCase {

    @Autowired private UserRepository userRepository;

    @Override
    public Boolean updateUserPassword(UpdateUserPasswordCommand command) {
        Objects.requireNonNull(command);
        String oldPassword = command.oldPassword().trim();
        String newPassword = command.newPassword().trim();
        if (!oldPassword.equals(newPassword)) return false;
        return userRepository.updatePassword(command.userId(), newPassword);
    }
}
