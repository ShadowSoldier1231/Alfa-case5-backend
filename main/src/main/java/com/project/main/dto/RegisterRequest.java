package com.project.main.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserStatus;
import java.time.LocalDate;

public class RegisterRequest{
    private Long id;
    private String email;
    private String username;
    private String password;
    private String middleName;
    private UserStatus status;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthdate;
    private String city;
    private String firstName;
    private String lastName;
    private GenderCode gender;

    public RegisterRequest(){

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



    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

    public Long getId() {
        return id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(Long id) {
        this.id = id;
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