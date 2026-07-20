package com.project.main.dto;


import jakarta.validation.constraints.NotNull;

public class VerificationRequest {

    @NotNull(message = "user id is required")
    private Long userId;
    private Long verification;

    VerificationRequest(){

    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getVerification() {
        return verification;
    }

    public void setVerification(Long verification) {
        this.verification = verification;
    }
}
