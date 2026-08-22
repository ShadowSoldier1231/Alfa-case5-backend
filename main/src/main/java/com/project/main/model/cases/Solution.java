package com.project.main.model.cases;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long solutionId;

    private Long caseId;
    private Long userId;
    private Long rating;

    @Column(columnDefinition = "TEXT")
    private String solutionText;

    @Column(columnDefinition = "TEXT")
    private String solutionResponse;

    private Integer solvedMin;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getSolvedMin() {
        return solvedMin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setSolvedMin(Integer solvedMin) {
        this.solvedMin = solvedMin;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public Long getCaseId() {
        return caseId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRating() {
        return rating;
    }

    public void setSolutionId(Long solutionId) {
        this.solutionId = solutionId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSolutionResponse() {
        return solutionResponse;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    public String getSolutionText() {
        return solutionText;
    }

    public void setSolutionResponse(String solutionResponse) {
        this.solutionResponse = solutionResponse;
    }

    public void setSolutionText(String solutionText) {
        this.solutionText = solutionText;
    }

}
