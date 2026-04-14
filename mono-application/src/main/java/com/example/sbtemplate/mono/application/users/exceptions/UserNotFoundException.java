package com.example.sbtemplate.mono.application.users.exceptions;

import java.util.UUID;

import com.example.sbtemplate.mono.application.shared.exceptions.AppRuntimeException;
import com.example.sbtemplate.mono.application.shared.exceptions.ServerErrorCodes;

public class UserNotFoundException extends AppRuntimeException {
    public UserNotFoundException(UUID id) {
        super(
            "User not found with id: " + id,
            ServerErrorCodes.NOT_FOUND
        );
    }
}
