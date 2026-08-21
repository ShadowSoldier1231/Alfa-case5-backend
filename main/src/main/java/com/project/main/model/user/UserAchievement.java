package com.project.main.model.user;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "user_achievement", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_achievement", columnNames = {"user_id", "achievement_id"})
})
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "achievement_id", nullable = false)
    private Long achievementId;

    @Column(name = "obtained_at", nullable = false)
    private LocalDateTime obtainedAt;

    public UserAchievement() {
    }

    public UserAchievement(Long userId, Long achievementId, LocalDateTime obtainedAt) {
        this.userId = userId;
        this.achievementId = achievementId;
        this.obtainedAt = obtainedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(Long achievementId) {
        this.achievementId = achievementId;
    }

    public LocalDateTime getObtainedAt() {
        return obtainedAt;
    }

    public void setObtainedAt(LocalDateTime obtainedAt) {
        this.obtainedAt = obtainedAt;
    }
}