package com.project.main.exception;

import org.springframework.http.HttpStatus;

public class InvalidSessionException extends ApiException {
    public InvalidSessionException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}