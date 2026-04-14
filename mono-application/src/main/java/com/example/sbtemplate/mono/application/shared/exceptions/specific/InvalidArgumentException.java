package com.example.sbtemplate.mono.application.shared.exceptions.specific;

import com.example.sbtemplate.mono.application.shared.exceptions.AppRuntimeException;
import com.example.sbtemplate.mono.application.shared.exceptions.ServerErrorCodes;

public class InvalidArgumentException extends AppRuntimeException {
    public InvalidArgumentException(String message) {
        super(
            message,
            ServerErrorCodes.BAD_REQUEST
        );
    }
}
