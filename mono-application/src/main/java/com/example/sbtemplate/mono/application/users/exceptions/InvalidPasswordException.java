package com.example.sbtemplate.mono.application.users.exceptions;

import com.example.sbtemplate.mono.application.shared.exceptions.AppRuntimeException;
import com.example.sbtemplate.mono.application.shared.exceptions.ServerErrorCodes;

public class InvalidPasswordException extends AppRuntimeException {
    public InvalidPasswordException() {
        super(
            "Bad password",
            ServerErrorCodes.BAD_REQUEST
        );
    }
}
