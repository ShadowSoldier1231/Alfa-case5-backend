package com.project.main.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ForgotPasswordConfirmRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotNull(message = "Verification code is required")
    private Long code;

    @NotBlank(message = "New password cannot be empty")
    private String newPassword;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getCode() { return code; }
    public void setCode(Long code) { this.code = code; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}