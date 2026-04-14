package com.alvaromg.portfolio.application.roles.exceptions;

import com.alvaromg.portfolio.application.shared.exceptions.AppRuntimeException;
import com.alvaromg.portfolio.application.shared.exceptions.ServerErrorCodes;

public class RoleNotFoundException extends AppRuntimeException {
    public RoleNotFoundException(String id) {
        super(
            "Role not found with id: " + id,
            ServerErrorCodes.NOT_FOUND
        );
    }
}
