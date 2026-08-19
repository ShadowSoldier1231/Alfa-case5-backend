package com.project.main.model;

import com.project.main.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDateTime;





@Entity
@Table(
        name = "user_setup",
        indexes = {
                @Index(name = "idx_user_setup_creation_date", columnList = "creation_date")
        }
)
public class UserSetup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "telegram_id", unique = true)
    private Long telegramId;
    private boolean isVerified;
    private  String password;
    @Column(unique = true)
    private  String username;
    @Column(unique = true)
    @Email(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    private  String email;
    private LocalDateTime creationDate;
    private LocalDateTime bannedUntil;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role;



    public UserSetup(String password, String username, String email,  UserRole role, LocalDateTime bannedUntil, boolean isVerified){
        this.username = username;
        this.password = password;
        this.creationDate = LocalDateTime.now();
        this.email = email;
        this.role = role;
        this.bannedUntil = bannedUntil;
        this.isVerified = isVerified;


    }
    public UserSetup(){
    }

    public UserSetup(String password, String username){
        this.password = password;
        this.username = username;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
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


    public void setEmail(String email) {
        if(email != null && !EmailValidator.getInstance(true).isValid(email)) {
            throw new IllegalArgumentException("This email address is invalid");
        }
        this.email = email;
    }



    public void  setPassword(String password){
        this.password = password;
    }



}