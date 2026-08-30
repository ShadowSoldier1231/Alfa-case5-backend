package com.project.main.dto.learing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class QuestionUpsertDto {

    @NotBlank(message = "Question text cannot be empty")
    private String text;

    @NotNull(message = "Question position is required")
    private Integer position;

    private Boolean isActive;

    @NotEmpty(message = "Options list cannot be empty")
    @Valid
    private List<OptionUpsertDto> options;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public List<OptionUpsertDto> getOptions() { return options; }
    public void setOptions(List<OptionUpsertDto> options) { this.options = options; }
}