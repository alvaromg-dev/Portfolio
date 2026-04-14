package com.example.sbtemplate.mono.application.roles.exceptions;

import com.example.sbtemplate.mono.application.shared.exceptions.AppRuntimeException;
import com.example.sbtemplate.mono.application.shared.exceptions.ServerErrorCodes;

public class RoleNotFoundException extends AppRuntimeException {
    public RoleNotFoundException(String id) {
        super(
            "Role not found with id: " + id,
            ServerErrorCodes.NOT_FOUND
        );
    }
}
