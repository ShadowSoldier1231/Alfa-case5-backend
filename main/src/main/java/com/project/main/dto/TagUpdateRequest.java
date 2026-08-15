package com.project.main.dto;

import jakarta.validation.constraints.Size;

public class TagUpdateRequest {

    @Size(min = 1, max = 100, message = "Tag name must be between 1 and 100 characters")
    private String name;

    private Boolean active;

    public TagUpdateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}