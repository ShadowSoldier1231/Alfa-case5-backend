package com.project.main.dto.integration;

import java.time.Instant;

public class SolvingStatusResponse {
    private boolean active;
    private Instant timestamp;

    public SolvingStatusResponse() {
    }

    public SolvingStatusResponse(boolean active, Instant timestamp) {
        this.active = active;
        this.timestamp = timestamp;
    }

    public boolean isActive() {
        return active;
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