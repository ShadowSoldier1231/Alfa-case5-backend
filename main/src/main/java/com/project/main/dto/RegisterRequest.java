package com.project.main.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserStatus;
import com.project.main.enums.ValidationMethod;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class RegisterRequest{

    @Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    private String email;
    private String username;
    private String password;
    private String middleName;
    private UserStatus status;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthdate;
    private Long cityId;
    private String firstName;
    private String lastName;
    private GenderCode gender;
    @NotNull(message = "Validation method is required")
    private ValidationMethod validationMethod;

    public RegisterRequest(){

    }

    public ValidationMethod getValidationMethod() {
        return validationMethod;
    }

    public void setValidationMethod(ValidationMethod validationMethod) {
        this.validationMethod = validationMethod;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public GenderCode getGender() {
        return gender;
    }

    public void setGender(GenderCode gender) {
        this.gender = gender;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }


    public UserStatus getStatus() {
        return status;
    }



    public String getEmail() {
        return email;
    }



    public void setEmail(String email) {
        this.email = email;
    }



    public void setUsername(String username){
        this.username = username;
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}