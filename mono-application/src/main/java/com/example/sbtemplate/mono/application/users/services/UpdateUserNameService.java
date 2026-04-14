package com.example.sbtemplate.mono.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sbtemplate.mono.application.users.exceptions.UserNotFoundException;
import com.example.sbtemplate.mono.application.users.ports.in.UpdateUserNameUseCase;
import com.example.sbtemplate.mono.application.users.ports.out.UserRepository;
import com.example.sbtemplate.mono.domain.model.User;

@Service
@Transactional
public class UpdateUserNameService implements UpdateUserNameUseCase {

    @Autowired private UserRepository userRepository;

    @Override
    public User updateUserName(UpdateUserNameCommand command) {
        String givenNames = command.givenNames().trim();
        String familyNames = command.familyNames().trim();
        return userRepository.updateName(command.userId(), givenNames, familyNames)
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
    }
}
