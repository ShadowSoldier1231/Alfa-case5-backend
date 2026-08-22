package com.project.main.dto.event;

public class SolutionSubmittedEvent {
    private Long userId;
    private Long caseId;
    private Long rating;
    private Integer solvedMin;

    public SolutionSubmittedEvent(){

    }

    public SolutionSubmittedEvent(Long userId, Long caseId, Long rating, Integer solvedMin) {
        this.userId = userId;
        this.caseId = caseId;
        this.rating = rating;
        this.solvedMin = solvedMin;
    }
    public Integer getSolvedMin() {
        return solvedMin;
    }

    public void setSolvedMin(Integer solvedMin) {
        this.solvedMin = solvedMin;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    public Long getRating() {
        return rating;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
