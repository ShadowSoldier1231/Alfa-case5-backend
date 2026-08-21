package com.project.main.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class UserListItem {
    private Long id;
    private String username;
    private String email;
    private String nickName;
    private String role;
    private String status;

    @JsonProperty("isVerified")
    private boolean isVerified;

    private LocalDateTime bannedUntil;

    public UserListItem(Long id, String username, String email, String nickName,
                             String role, String status, boolean isVerified, LocalDateTime bannedUntil) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickName = nickName;
        this.role = role;
        this.status = status;
        this.isVerified = isVerified;
        this.bannedUntil = bannedUntil;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getNickName() { return nickName; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public boolean isVerified() { return isVerified; }
    public LocalDateTime getBannedUntil() { return bannedUntil; }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBannedUntil(LocalDateTime bannedUntil) {
        this.bannedUntil = bannedUntil;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}