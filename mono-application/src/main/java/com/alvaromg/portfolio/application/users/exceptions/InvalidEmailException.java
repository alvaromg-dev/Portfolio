package com.alvaromg.portfolio.application.users.exceptions;

import com.alvaromg.portfolio.application.shared.exceptions.AppRuntimeException;
import com.alvaromg.portfolio.application.shared.exceptions.ServerErrorCodes;

public class InvalidEmailException extends AppRuntimeException {
    public InvalidEmailException(String email) {
        super(
            "Bad email: " + email,
            ServerErrorCodes.BAD_REQUEST
        );
    }
}
