package com.project.main.dto;

import com.project.main.model.Views;
import com.fasterxml.jackson.annotation.JsonView;



public class RegisterResult{
    @JsonView(Views.RegisterResultPartial.class)
    private boolean success;
    @JsonView(Views.RegisterResultPartial.class)
    private String errorText;
    @JsonView(Views.RegisterResultFull.class)
    private String telegramUrl;
    @JsonView(Views.RegisterResultId.class)
    private Long id;

    public RegisterResult(boolean success, String ErrorText, Long id){
        this.success = success;
        this.errorText = ErrorText;
        this.id = id;
    }
    public RegisterResult(Long id){
        this.id = id;
    }
    public RegisterResult(boolean success, String ErrorText){
        this.success = success;
        this.errorText = ErrorText;
    }

    public RegisterResult(boolean success, String ErrorText, String telegramUrl, Long id){
        this.success = success;
        this.errorText = ErrorText;
        this.telegramUrl = telegramUrl;
        this.id = id;
    }


    public RegisterResult (){
    }

    public String getTelegramUrl() {
        return telegramUrl;
    }

    public void setTelegramUrl(String telegramUrl) {
        this.telegramUrl = telegramUrl;
    }


    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public String getErrorText(){
        return errorText;
    }
    public void setSuccess(boolean success){
        this.success = success;
    }
    public boolean getSuccess(){
        return success;
    }

}