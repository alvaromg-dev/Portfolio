package com.alvaromg.portfolio.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alvaromg.portfolio.application.users.ports.in.UpdateUserAvatarUseCase;
import com.alvaromg.portfolio.application.users.ports.out.UserRepository;

@Service
@Transactional
public class UpdateUserAvatarService implements UpdateUserAvatarUseCase {

    @Autowired private UserRepository userRepository;

    @Override
    public boolean updateUserAvatar(UpdateUserAvatarCommand command) {
        return userRepository.updateAvatar(command.userId(), command.avatar());
    }
}
