package com.project.main.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.main.enums.UserRole;
import com.project.main.enums.UserStatus;

import java.time.LocalDate;

import com.project.main.enums.*;
import jakarta.validation.constraints.Email;


public class AdminUserCreateRequest {


    private String username;
    private String password;
    @Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Invalid email format")
    private String email;

    private String firstName;
    private String lastName;
    private String middleName;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthdate;
    private UserStatus status;
    private Long cityId;
    private GenderCode gender;
    private UserRole role;

    public AdminUserCreateRequest() {
    }


    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }

    public GenderCode getGender() { return gender; }
    public void setGender(GenderCode gender) { this.gender = gender; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

}
