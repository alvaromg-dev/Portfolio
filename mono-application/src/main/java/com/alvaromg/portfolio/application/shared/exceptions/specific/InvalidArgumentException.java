package com.alvaromg.portfolio.application.shared.exceptions.specific;

import com.alvaromg.portfolio.application.shared.exceptions.AppRuntimeException;
import com.alvaromg.portfolio.application.shared.exceptions.ServerErrorCodes;

public class InvalidArgumentException extends AppRuntimeException {
    public InvalidArgumentException(String message) {
        super(
            message,
            ServerErrorCodes.BAD_REQUEST
        );
    }
}
