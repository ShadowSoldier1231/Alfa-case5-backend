package com.project.main.repository.projection;

public interface CaseTagRow {
    Long getCase_id();
    Long getTag_id();
    String getTag_name();
    Long getTag_case_count();
}