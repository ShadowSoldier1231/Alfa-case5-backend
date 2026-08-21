package com.project.main.dto.auth;

public class ResetPasswordRequest {
    private String oldPassword;
    private  String newPassword;
    public ResetPasswordRequest(){

    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
