package com.alvaromg.portfolio.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alvaromg.portfolio.application.users.exceptions.UserNotFoundException;
import com.alvaromg.portfolio.application.users.ports.in.UpdateUserNameUseCase;
import com.alvaromg.portfolio.application.users.ports.out.UserRepository;
import com.alvaromg.portfolio.domain.model.User;

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
