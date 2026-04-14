package com.example.sbtemplate.mono.application.users.exceptions;

import com.example.sbtemplate.mono.application.shared.exceptions.AppRuntimeException;
import com.example.sbtemplate.mono.application.shared.exceptions.ServerErrorCodes;

public class InvalidFamilyNamesException extends AppRuntimeException {
    public InvalidFamilyNamesException(String familyNames) {
        super(
            "Bad family names: " + familyNames,
            ServerErrorCodes.BAD_REQUEST
        );
    }
}
