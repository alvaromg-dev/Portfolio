package com.example.sbtemplate.mono.application.shared.exceptions;

public class AppRuntimeException extends RuntimeException {

    private final int errorCode;

    public AppRuntimeException(String ex, int serverErrorCode) {
        super(ex);
        this.errorCode = serverErrorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
