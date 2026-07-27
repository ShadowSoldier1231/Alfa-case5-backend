package com.project.main.dto;

import com.project.main.enums.Difficulty;

public class CaseUpdateRequest {

    private String slug;
    private String title;
    private String titleEn;
    private String description;
    private String fullDescription;
    private Difficulty difficulty;
    private Integer averageSolveMin;
    private String promptContextEn;
    private Boolean isActive;
    private Boolean removePdf;
    private Boolean removeIcon;

    public CaseUpdateRequest(){}

    public CaseUpdateRequest(String slug, String title, String titleEn,
                             String description, String fullDescription, Difficulty difficulty,
                             Integer averageSolveMin, String promptContextEn, Boolean isActive,
                             Boolean removePdf, Boolean removeIcon)
    {
        this.slug = slug;
        this.title = title;
        this.titleEn = titleEn;
        this.description = description;
        this.fullDescription = fullDescription;
        this.difficulty = difficulty;
        this.averageSolveMin = averageSolveMin;
        this.promptContextEn = promptContextEn;
        this.isActive = isActive;
        this.removePdf = removePdf;
        this.removeIcon = removeIcon;
    }

    public String getDescription() {
        return description;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public Boolean getActive() {
        return isActive;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public void setPromptContextEn(String promptContextEn) {
        this.promptContextEn = promptContextEn;
    }

    public String getPromptContextEn() {
        return promptContextEn;
    }

    public void setAverageSolveMin(Integer averageSolveMin) {
        this.averageSolveMin = averageSolveMin;
    }

    public Integer getAverageSolveMin() {
        return averageSolveMin;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }


    public Boolean getRemovePdf() { return removePdf; }
    public void setRemovePdf(Boolean removePdf) { this.removePdf = removePdf; }
    public Boolean getRemoveIcon() { return removeIcon; }
    public void setRemoveIcon(Boolean removeIcon) { this.removeIcon = removeIcon; }

}