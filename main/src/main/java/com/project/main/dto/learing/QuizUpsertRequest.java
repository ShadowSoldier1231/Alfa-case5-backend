package com.project.main.dto.learing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class QuizUpsertRequest {

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 255, message = "Title too long (max 255)")
    private String title;

    @NotNull(message = "isActive is required")
    private Boolean isActive;

    @NotEmpty(message = "Questions list cannot be empty")
    @Valid
    private List<QuestionUpsertDto> questions;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public List<QuestionUpsertDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionUpsertDto> questions) { this.questions = questions; }
}