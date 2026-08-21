package com.project.main.model.user;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favourite_cases")
public class UserFavoriteCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long caseId;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    public UserFavoriteCase(){

    }
    public UserFavoriteCase(Long userId, Long caseId){
        this.userId = userId;
        this.caseId = caseId;
    }

    public Long getId() {
        return id;
    }

    public Long getCaseId() {
        return caseId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
