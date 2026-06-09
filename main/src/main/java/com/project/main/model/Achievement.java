package com.project.main.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String achievementText;
    private Long scoreValue;

    public  Achievement(){

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

    public String getAchievementText() {
        return achievementText;
    }

    public void setAchievementText(String achievementText) {
        this.achievementText = achievementText;
    }
}
