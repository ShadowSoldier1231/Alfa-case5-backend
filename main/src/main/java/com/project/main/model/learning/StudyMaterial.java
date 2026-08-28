package com.project.main.model.learning;

import jakarta.persistence.*;

@Entity
@Table(name = "study_material", indexes = {
        @Index(name = "idx_study_material_case_id", columnList = "case_id")
})
public class StudyMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public StudyMaterial() {
    }

    public StudyMaterial(Long caseId, String text) {
        this.caseId = caseId;
        this.text = text;
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