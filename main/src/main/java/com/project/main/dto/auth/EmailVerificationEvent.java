package com.project.main.dto.auth;

public class EmailVerificationEvent {
    private final String email;
    private final long code;

    public EmailVerificationEvent(String email, long code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() { return email; }
    public long getCode() { return code; }
}
