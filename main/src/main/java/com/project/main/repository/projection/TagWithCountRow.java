package com.project.main.repository.projection;

public interface TagWithCountRow {
    Long getId();
    String getName();
    Boolean getIs_active();
    Long getCase_count();
}