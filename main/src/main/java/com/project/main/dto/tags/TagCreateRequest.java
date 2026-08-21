package com.project.main.dto.tags;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TagCreateRequest {

    @NotBlank(message = "Tag name cannot be empty")
    @Size(min = 1, max = 100, message = "Tag name must be between 1 and 100 characters")
    private String name;

    public TagCreateRequest() {
    }

    public TagCreateRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}