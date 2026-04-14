package com.example.sbtemplate.mono.application.users.services;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sbtemplate.mono.application.users.exceptions.UserNotFoundException;
import com.example.sbtemplate.mono.application.users.ports.in.DeleteUserUseCase;
import com.example.sbtemplate.mono.application.users.ports.out.UserRepository;
import com.example.sbtemplate.mono.domain.model.User;

@Service
@Transactional
public class DeleteUserService implements DeleteUserUseCase {

    @Autowired private UserRepository userRepository;

    @Override
    public boolean delete(DeleteUserCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        user.setDeletedAt(LocalDateTime.now());
        userRepository.update(user);

        User deletedUser = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
        return deletedUser.getDeletedAt() != null;
    }
}
