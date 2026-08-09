package com.project.main.exception;

import org.springframework.http.HttpStatus;

public class InvalidSessionException extends ApiException {
    private String token;
    public InvalidSessionException(String message, String token) {
        super(message, HttpStatus.UNAUTHORIZED);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}