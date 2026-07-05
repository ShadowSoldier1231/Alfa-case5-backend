package com.project.main.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;



@Entity
public class LeaderboardUser {

    @Id
    private Long userId;
    private Long score;
    private Long warningsCount;
    private Long placement;
    private Long banCount;


    public LeaderboardUser(){

    }
    public LeaderboardUser(Long userId, Long score, Long warningsCount, Long placement,  Long banCount){

        this.userId = userId;
        this.score = score;
        this.warningsCount = warningsCount;
        this.placement = placement;
        this.banCount = banCount;
    }

    public Long getBanCount() {
        return banCount;
    }

    public void setBanCount(Long banCount) {
        this.banCount = banCount;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Long getPlacement() {
        return placement;
    }

    public void setPlacement(Long placement) {
        this.placement = placement;
    }

    public Long getWarningsCount() {
        return warningsCount;
    }

    public void setWarningsCount(Long warningsCount) {
        this.warningsCount = warningsCount;
    }
}