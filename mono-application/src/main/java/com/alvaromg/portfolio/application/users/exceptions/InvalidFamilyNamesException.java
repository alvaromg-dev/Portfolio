package com.alvaromg.portfolio.application.users.exceptions;

import com.alvaromg.portfolio.application.shared.exceptions.AppRuntimeException;
import com.alvaromg.portfolio.application.shared.exceptions.ServerErrorCodes;

public class InvalidFamilyNamesException extends AppRuntimeException {
    public InvalidFamilyNamesException(String familyNames) {
        super(
            "Bad family names: " + familyNames,
            ServerErrorCodes.BAD_REQUEST
        );
    }
}
