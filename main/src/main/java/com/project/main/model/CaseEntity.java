package com.project.main.model;

import com.project.main.enums.Difficulty;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cases")
public class CaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "title_en", length = 255)
    private String titleEn;

    @Column(length = 500)
    private String description;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Column(name = "average_solve_min")
    private Integer averageSolveMin;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "prompt_context_en", columnDefinition = "TEXT")
    private String promptContextEn;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;



    @Column(name = "views_count", nullable = false)
    private Integer viewsCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaseTag> caseTags = new ArrayList<>();

    public CaseEntity(){

    }

    public CaseEntity(String slug, String title, String titleEn, String description, String fullDescription,
                      Difficulty difficulty, Integer averageSolveMin, String pdfUrl, String iconUrl,
                      String promptContextEn, Boolean isActive,  Integer viewsCount) {
        this.slug = slug;
        this.title = title;
        this.titleEn = titleEn;
        this.description = description;
        this.fullDescription = fullDescription;
        this.difficulty = difficulty;
        this.averageSolveMin = averageSolveMin;
        this.pdfUrl = pdfUrl;
        this.iconUrl = iconUrl;
        this.promptContextEn = promptContextEn;
        this.isActive = isActive != null ? isActive : true;
        this.viewsCount = viewsCount != null ? viewsCount : 0;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getAverageSolveMin() {
        return averageSolveMin;
    }

    public void setAverageSolveMin(Integer averageSolveMin) {
        this.averageSolveMin = averageSolveMin;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleEn() {
        return titleEn;
    }


    public Integer getViewsCount() {
        return viewsCount;
    }

    public List<CaseTag> getCaseTags() {
        return caseTags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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

    public String getPromptContextEn() {
        return promptContextEn;
    }

    public void setCaseTags(List<CaseTag> caseTags) {
        this.caseTags = caseTags;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setPromptContextEn(String promptContextEn) {
        this.promptContextEn = promptContextEn;
    }


    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }


    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void addTag(Tag tag) {
        CaseTag caseTag = new CaseTag();
        caseTag.setCaseEntity(this);
        caseTag.setTag(tag);
        this.caseTags.add(caseTag);
        tag.getCaseTags().add(caseTag);
    }


    public void removeTag(Tag tag) {
        CaseTag toRemove = caseTags.stream()
                .filter(ct -> ct.getTag().equals(tag))
                .findFirst()
                .orElse(null);
        if (toRemove != null) {
            this.caseTags.remove(toRemove);
            tag.getCaseTags().remove(toRemove);
            toRemove.setCaseEntity(null);
            toRemove.setTag(null);
        }
    }
}