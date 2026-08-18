package com.project.main.dto;

import com.project.main.enums.Difficulty;

import java.time.LocalDateTime;
import java.util.List;

public class FavoriteCaseDto {
    private Long id;
    private String slug;
    private String title;
    private String titleEn;
    private String description;
    private String fullDescription;
    private Difficulty difficulty;
    private Integer averageSolveMin;
    private String pdfUrl;
    private String iconUrl;
    private Integer viewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime addedAt;
    private List<CasePublicDto.TagInfo> tags;

    public FavoriteCaseDto() {

    }

    public FavoriteCaseDto(Long id, String slug, String title, String titleEn, String description,
                           String fullDescription, Difficulty difficulty, Integer averageSolveMin,
                           String pdfUrl, String iconUrl, Integer viewsCount, LocalDateTime createdAt,
                           LocalDateTime updatedAt, LocalDateTime addedAt, List<CasePublicDto.TagInfo> tags) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.titleEn = titleEn;
        this.description = description;
        this.fullDescription = fullDescription;
        this.difficulty = difficulty;
        this.averageSolveMin = averageSolveMin;
        this.pdfUrl = pdfUrl;
        this.iconUrl = iconUrl;
        this.viewsCount = viewsCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.addedAt = addedAt;
        this.tags = tags != null ? List.copyOf(tags) : null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Integer getAverageSolveMin() {
        return averageSolveMin;
    }

    public Integer getViewsCount() {
        return viewsCount;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CasePublicDto.TagInfo> getTags() {
        return tags;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public void setAverageSolveMin(Integer averageSolveMin) {
        this.averageSolveMin = averageSolveMin;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public void setTags(List<CasePublicDto.TagInfo> tags) {
        this.tags = tags;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }
}

