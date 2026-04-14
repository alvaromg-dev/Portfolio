package com.example.sbtemplate.mono.application.users.exceptions;

import com.example.sbtemplate.mono.application.shared.exceptions.AppRuntimeException;
import com.example.sbtemplate.mono.application.shared.exceptions.ServerErrorCodes;

public class InvalidEmailException extends AppRuntimeException {
    public InvalidEmailException(String email) {
        super(
            "Bad email: " + email,
            ServerErrorCodes.BAD_REQUEST
        );
    }
}
