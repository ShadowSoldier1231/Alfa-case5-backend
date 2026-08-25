package com.project.main.model.cases;

import jakarta.persistence.*;

@Entity
@Table(
        name = "case_rating",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_case_rating_user_id_case_id",
                        columnNames = {"user_id", "case_id"}
                )
        },
        indexes = {
                @Index(name = "idx_case_rating_case_id", columnList = "case_id")
        }
)
public class CaseRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(nullable = false)
    private Long rating;

    public CaseRating() {
    }

    public CaseRating(Long userId, Long caseId, Long rating) {
        this.userId = userId;
        this.caseId = caseId;
        this.rating = rating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }
}