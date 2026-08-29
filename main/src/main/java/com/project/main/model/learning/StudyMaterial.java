package com.project.main.model.learning;

import jakarta.persistence.*;

@Entity
@Table(
        name = "study_material",
        indexes = {
                @Index(name = "idx_study_material_case_id", columnList = "case_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_study_material_case_id_position",
                        columnNames = {"position", "case_id"}
                )
        }
)
public class StudyMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public StudyMaterial() {
    }

    public StudyMaterial(Long caseId, String text, String title, Integer position, Boolean isActive) {
        this.caseId = caseId;
        this.text = text;
        this.title = title;
        this.position = position;
        this.isActive = isActive != null ? isActive : true;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Boolean getActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }
}