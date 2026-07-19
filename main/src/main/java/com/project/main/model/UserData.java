package com.project.main.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserStatus;
import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
public class UserData {
    @Id
    private Long id;
    private Long cityId;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthdate;
    private  String firstName;
    private String middleName;
    private String lastName;
    private String nickName;
    @Enumerated(EnumType.STRING)
    private GenderCode gender;

    public UserData(){

    }

    public UserData(Long id, String firstName, String lastName,
                    LocalDate birthdate, UserStatus status,  Long cityId, String middleName,
                     GenderCode gender, String nickName){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.status = status;
        this.cityId = cityId;
        this.middleName = middleName;
        this.gender = gender;
        this.nickName = nickName;

    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
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

    public Long getId() {
        return id;
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

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }


    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }


    public GenderCode getGender() {
        return gender;
    }

    public void setGender(GenderCode gender) {
        this.gender = gender;
    }
}
