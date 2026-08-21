package com.project.main.dto.event;

public class ForgotPasswordInitEvent {
    private final String email;
    private final long code;
    private final String clientIp;

    public ForgotPasswordInitEvent(String email, long code, String clientIp) {
        this.email = email;
        this.code = code;
        this.clientIp = clientIp;
    }

    public String getEmail() { return email; }
    public long getCode() { return code; }
    public String getClientIp() { return clientIp; }
}