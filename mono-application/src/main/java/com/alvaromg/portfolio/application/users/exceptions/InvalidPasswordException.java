package com.alvaromg.portfolio.application.users.exceptions;

import com.alvaromg.portfolio.application.shared.exceptions.AppRuntimeException;
import com.alvaromg.portfolio.application.shared.exceptions.ServerErrorCodes;

public class InvalidPasswordException extends AppRuntimeException {
    public InvalidPasswordException() {
        super(
            "Bad password",
            ServerErrorCodes.BAD_REQUEST
        );
    }
}
