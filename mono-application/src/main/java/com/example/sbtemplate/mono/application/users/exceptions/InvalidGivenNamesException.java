package com.example.sbtemplate.mono.application.users.exceptions;

import com.example.sbtemplate.mono.application.shared.exceptions.AppRuntimeException;
import com.example.sbtemplate.mono.application.shared.exceptions.ServerErrorCodes;

public class InvalidGivenNamesException extends AppRuntimeException {
    public InvalidGivenNamesException(String givenNames) {
        super(
            "Bad given names: " + givenNames,
            ServerErrorCodes.BAD_REQUEST
        );
    }
}
