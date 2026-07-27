package com.project.main.dto;

import com.project.main.enums.Difficulty;

public class CaseCreateRequest {

    private String slug;
    private String title;
    private String titleEn;
    private String description;
    private String fullDescription;
    private Difficulty difficulty;
    private Integer averageSolveMin;
    private String promptContextEn;
    private Boolean isActive;


    public CaseCreateRequest() {
    }

    public CaseCreateRequest(String slug, String title, String titleEn, String description,
                             String fullDescription, Difficulty difficulty, Integer averageSolveMin,
                             String promptContextEn, Boolean isActive) {
        this.slug = slug;
        this.title = title;
        this.titleEn = titleEn;
        this.description = description;
        this.fullDescription = fullDescription;
        this.difficulty = difficulty;
        this.averageSolveMin = averageSolveMin;
        this.promptContextEn = promptContextEn;
        this.isActive = isActive;

    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getActive() {
        return isActive;
    }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFullDescription() { return fullDescription; }
    public void setFullDescription(String fullDescription) { this.fullDescription = fullDescription; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public Integer getAverageSolveMin() { return averageSolveMin; }
    public void setAverageSolveMin(Integer averageSolveMin) { this.averageSolveMin = averageSolveMin; }

    public String getPromptContextEn() { return promptContextEn; }
    public void setPromptContextEn(String promptContextEn) { this.promptContextEn = promptContextEn; }


}
