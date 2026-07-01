package com.project.main.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InputUser{
    private Long id;
    private String email;
    private String username;
    private String oldPassword;
    private  String newPassword;
    private String password;
    private LocalDateTime creationDate;
    private Long cityId;
    private String middleName;
    private UserStatus status;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthdate;
    private String city;
    private String firstName;
    private String lastName;
    private Long profilePictureId;
    private GenderCode gender;

    public InputUser(){

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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCreationDate() {
        this.creationDate = LocalDateTime.now();
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
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
    public void setOldPassword(String oldPassword){
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
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

    public void setProfilePictureId(Long profilePictureId) {
        this.profilePictureId = profilePictureId;
    }

    public Long getProfilePictureId() {
        return profilePictureId;
    }
}