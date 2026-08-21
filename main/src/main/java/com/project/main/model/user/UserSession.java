package com.project.main.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;


@Entity
public class UserSession {

    @Id
    private String token;
    private Long userId;
    private LocalDateTime expiryDate;

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }
}