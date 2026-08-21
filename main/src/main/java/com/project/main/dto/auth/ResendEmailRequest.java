package com.project.main.dto.auth;

import com.project.main.enums.ValidationMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResendEmailRequest {

    @NotBlank(message = "Email is required")
    @Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
    @NotNull(message = "Validation method is required")
    private ValidationMethod validationMethod;

    public ResendEmailRequest(){

    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public ValidationMethod getValidationMethod() {
        return validationMethod;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setValidationMethod(ValidationMethod validationMethod) {
        this.validationMethod = validationMethod;
    }
}
