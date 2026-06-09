package com.project.main.dto;

public class RegisterResult{
    private boolean success;
    private String ErrorText;

    public RegisterResult(boolean success, String ErrorText){
        this.success = success;
        this.ErrorText = ErrorText;
    }
    public RegisterResult (){
    }

    public void setErrorText(String errorText) {
        ErrorText = errorText;
    }

    public String getErrorText(){
        return ErrorText;
    }
    public void setSuccess(boolean success){
        this.success = success;
    }
    public boolean getSuccess(){
        return success;
    }

}