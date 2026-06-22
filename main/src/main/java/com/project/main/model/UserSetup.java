package com.project.main.model;

import com.project.main.enums.UserRole;
import jakarta.persistence.*;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDateTime;





@Entity
public class UserSetup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "telegram_id", unique = true)
    private Long telegramId;
    @Column(name = "telegram_verification_token", length = 64, unique = true)
    private String telegramVerificationToken;
    private  String password;
    private  String username;
    private  String email;
    private LocalDateTime creationDate;
    private LocalDateTime bannedUntil;

    @Enumerated(EnumType.STRING)
    private UserRole role;



    public UserSetup(String password, String username, String email,  UserRole role, LocalDateTime bannedUntil){
        this.username = username;
        this.password = password;
        this.creationDate = LocalDateTime.now();
        this.email = email;
        this.role = role;
        this.bannedUntil = bannedUntil;



    }
    public UserSetup(){
    }

    public UserSetup(String password, String username){
        this.password = password;
        this.username = username;
    }

    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }

    public void setBannedUntil(LocalDateTime bannedUntil) {
        this.bannedUntil = bannedUntil;
    }

    public UserRole getRole() {
        return role;
    }


    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword(){
        return password;
    }
    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public String getTelegramVerificationToken() {
        return telegramVerificationToken;
    }

    public void setTelegramVerificationToken(String telegramVerificationToken) {
        this.telegramVerificationToken = telegramVerificationToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public LocalDateTime getCreationDate(){
        return creationDate;
    }
    public void setCreationDate(LocalDateTime creationDate){
        this.creationDate = creationDate;
    }
    public void setCurrentTime(){
        this.creationDate = LocalDateTime.now();
    }


    public void setEmail(String  email) {
        if(EmailValidator.getInstance(true).isValid(email)) {
            this.email = email;
        }
    }



    public void  setPassword(String password){
        this.password = password;
    }



}