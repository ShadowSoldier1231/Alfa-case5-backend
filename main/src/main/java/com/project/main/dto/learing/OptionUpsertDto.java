package com.project.main.dto.learing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OptionUpsertDto {

    @NotBlank(message = "Option text cannot be empty")
    private String text;

    @NotNull(message = "Option position is required")
    private Integer position;

    @NotNull(message = "Option isCorrect is required")
    private Boolean isCorrect;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
}