package com.project.main.dto.event;

public class ForgotUsernameEvent {
    private final String email;
    private final String clientIp;

    public ForgotUsernameEvent(String email, String clientIp) {
        this.email = email;
        this.clientIp = clientIp;
    }

    public String getEmail() { return email; }
    public String getClientIp() { return clientIp; }
}