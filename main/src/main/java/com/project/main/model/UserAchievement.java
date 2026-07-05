package com.project.main.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;


@Entity
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long scoreValue;
    private LocalDateTime ObtainedAt;


    public  UserAchievement(){

    }

    public LocalDateTime getObtainedAt() {
        return ObtainedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setObtainedAt(LocalDateTime obtainedAt) {
        ObtainedAt = obtainedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(Long scoreValue) {
        this.scoreValue = scoreValue;
    }

}
