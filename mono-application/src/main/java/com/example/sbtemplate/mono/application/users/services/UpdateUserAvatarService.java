package com.example.sbtemplate.mono.application.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sbtemplate.mono.application.users.ports.in.UpdateUserAvatarUseCase;
import com.example.sbtemplate.mono.application.users.ports.out.UserRepository;

@Service
@Transactional
public class UpdateUserAvatarService implements UpdateUserAvatarUseCase {

    @Autowired private UserRepository userRepository;

    @Override
    public boolean updateUserAvatar(UpdateUserAvatarCommand command) {
        return userRepository.updateAvatar(command.userId(), command.avatar());
    }
}
