package com.project.main.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.main.enums.UserStatus;
import java.time.LocalDate;
import com.project.main.enums.*;
import java.time.LocalDateTime;

public class AdminUserUpdateRequest {


    private UserRole role;
    private LocalDateTime bannedUntil;
    private Boolean isVerified;

    @jakarta.validation.constraints.Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Invalid email format")
    private String email;


    private String firstName;
    private String lastName;
    private String middleName;
    private String nickName;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthdate;

    private UserStatus status;
    private Long cityId;
    private GenderCode gender;


    public AdminUserUpdateRequest() {
    }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public LocalDateTime getBannedUntil() { return bannedUntil; }
    public void setBannedUntil(LocalDateTime bannedUntil) { this.bannedUntil = bannedUntil; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }

    public GenderCode getGender() { return gender; }
    public void setGender(GenderCode gender) { this.gender = gender; }
}