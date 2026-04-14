package com.alvaromg.portfolio.application.users.exceptions;

import java.util.UUID;

import com.alvaromg.portfolio.application.shared.exceptions.AppRuntimeException;
import com.alvaromg.portfolio.application.shared.exceptions.ServerErrorCodes;

public class UserNotFoundException extends AppRuntimeException {
    public UserNotFoundException(UUID id) {
        super(
            "User not found with id: " + id,
            ServerErrorCodes.NOT_FOUND
        );
    }
}
