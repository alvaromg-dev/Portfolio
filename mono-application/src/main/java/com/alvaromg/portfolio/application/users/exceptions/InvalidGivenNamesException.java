package com.alvaromg.portfolio.application.users.exceptions;

import com.alvaromg.portfolio.application.shared.exceptions.AppRuntimeException;
import com.alvaromg.portfolio.application.shared.exceptions.ServerErrorCodes;

public class InvalidGivenNamesException extends AppRuntimeException {
    public InvalidGivenNamesException(String givenNames) {
        super(
            "Bad given names: " + givenNames,
            ServerErrorCodes.BAD_REQUEST
        );
    }
}
