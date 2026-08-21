package com.project.main.model.user;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
public class UserVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(name = "telegram_verification_token", length = 64, unique = true)
    private String telegramVerificationToken;

    private Long emailVerificationCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserVerification(){

    }
    public UserVerification(Long userId, String telegramVerificationToken){
        this.userId = userId;
        this.telegramVerificationToken = telegramVerificationToken;
    }

    public UserVerification(Long userId, Long emailVerificationCode){
        this.userId = userId;
        this.emailVerificationCode = emailVerificationCode;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmailVerificationCode() {
        return emailVerificationCode;
    }

    public String getTelegramVerificationToken() {
        return telegramVerificationToken;
    }

    public void setTelegramVerificationToken(String telegramVerificationToken) {
        this.telegramVerificationToken = telegramVerificationToken;
    }

    public void setEmailVerificationCode(Long emailVerificationCode) {
        this.emailVerificationCode = emailVerificationCode;
    }

}
