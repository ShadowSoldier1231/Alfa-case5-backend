package com.project.main.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
