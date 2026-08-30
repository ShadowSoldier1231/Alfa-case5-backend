package com.project.main.model.learning;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz", indexes = {
        @Index(name = "idx_quiz_material_id", columnList = "material_id")
        }
)
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(nullable = false)
    private String title;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Quiz() {
    }

    public Quiz(Long materialId, String title) {
        this.materialId = materialId;
        this.title = title;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Boolean getActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }
}