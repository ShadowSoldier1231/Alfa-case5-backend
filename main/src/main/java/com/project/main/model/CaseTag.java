package com.project.main.model;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;


@Entity
@Table(name = "case_tags")
public class CaseTag {

    @EmbeddedId
    private CaseTagId id = new CaseTagId();

    @MapsId("caseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseEntity caseEntity;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CaseTag() {
    }

    public CaseTag(CaseEntity caseEntity, Tag tag) {
        this.caseEntity = caseEntity;
        this.tag = tag;

        this.id = new CaseTagId(caseEntity.getId(), tag.getId());
    }



    public CaseTagId getId() {
        return id;
    }

    public void setId(CaseTagId id) {
        this.id = id;
    }

    public CaseEntity getCaseEntity() {
        return caseEntity;
    }

    public void setCaseEntity(CaseEntity caseEntity) {
        this.caseEntity = caseEntity;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}