package com.project.main.dto.auth;

import jakarta.validation.constraints.Email;

public class ChangeEmailRequest {

    @Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    private String email;

    ChangeEmailRequest(){

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
