package com.example.sbtemplate.mono.application.shared.exceptions.specific;

import com.example.sbtemplate.mono.application.shared.exceptions.AppRuntimeException;
import com.example.sbtemplate.mono.application.shared.exceptions.ServerErrorCodes;

public class UnauthorizedException extends AppRuntimeException {
    public UnauthorizedException(String resource) {
        super(
            "You are not authorized to access this resource: " + resource,
            ServerErrorCodes.UNAUTHORIZED
        );
    }
}
