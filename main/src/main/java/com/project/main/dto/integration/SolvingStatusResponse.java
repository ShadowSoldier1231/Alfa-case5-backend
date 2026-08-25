package com.project.main.dto.integration;

import java.time.Instant;

public class SolvingStatusResponse {
    private boolean active;
    private boolean completed;
    private Long bestRating;
    private Instant timestamp;

    public SolvingStatusResponse() {
    }

    public SolvingStatusResponse(boolean active, Instant timestamp, boolean completed, Long bestRating) {
        this.active = active;
        this.timestamp = timestamp;
        this.completed = completed;
        this.bestRating = bestRating;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Long getBestRating() {
        return bestRating;
    }

    public void setBestRating(Long bestRating) {
        this.bestRating = bestRating;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}