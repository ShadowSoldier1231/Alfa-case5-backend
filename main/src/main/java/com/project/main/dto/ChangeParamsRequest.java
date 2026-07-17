package com.project.main.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.main.enums.UserStatus;
import jakarta.validation.constraints.Email;
import java.time.LocalDate;

public class ChangeParamsRequest {



    private String firstName;
    private String lastName;
    private String middleName;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthdate;
    private Long cityId;
    private UserStatus status;

    public ChangeParamsRequest(){

    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }


    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public String getFirstName() {
        return firstName;
    }



    public LocalDate getBirthdate() {
        return birthdate;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }



    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }



}
