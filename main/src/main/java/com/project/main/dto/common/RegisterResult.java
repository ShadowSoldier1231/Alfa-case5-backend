package com.project.main.dto.common;

import com.project.main.model.common.Views;
import com.fasterxml.jackson.annotation.JsonView;



public class RegisterResult{
    @JsonView(Views.RegisterResultPartial.class)
    private boolean success;
    @JsonView(Views.RegisterResultPartial.class)
    private String errorText;
    @JsonView(Views.RegisterResultFull.class)
    private String verification;
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

    public RegisterResult(boolean success, String ErrorText, String verification, Long id){
        this.success = success;
        this.errorText = ErrorText;
        this.verification = verification;
        this.id = id;
    }


    public RegisterResult (){
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }


    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public String getErrorText(){
        return errorText;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setSuccess(boolean success){
        this.success = success;
    }
    public boolean getSuccess(){
        return success;
    }

}