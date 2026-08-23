package com.project.main.dto.event;

public class SolutionSubmittedEvent {
    private Long userId;
    private Long caseId;
    private Long rating;
    private Integer timeSpentMinutes;

    public SolutionSubmittedEvent(){

    }

    public SolutionSubmittedEvent(Long userId, Long caseId, Long rating, Integer timeSpentMinutes) {
        this.userId = userId;
        this.caseId = caseId;
        this.rating = rating;
        this.timeSpentMinutes = timeSpentMinutes;
    }
    public Integer getTimeSpentMinutes() {
        return timeSpentMinutes;
    }

    public void setTimeSpentMinutes(Integer timeSpentMinutes) {
        this.timeSpentMinutes = timeSpentMinutes;
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
