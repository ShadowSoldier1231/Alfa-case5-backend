package com.project.main.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CaseTagId implements Serializable {

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "tag_id")
    private Long tagId;

    public CaseTagId(){

    }
    public CaseTagId(Long caseId, Long tagId){
        this.tagId = tagId;
        this.caseId = caseId;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaseTagId that = (CaseTagId) o;
        return Objects.equals(caseId, that.caseId) &&
                Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseId, tagId);
    }
}