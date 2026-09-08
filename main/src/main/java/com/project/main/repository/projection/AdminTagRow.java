package com.project.main.repository.projection;

import java.time.LocalDateTime;

public interface AdminTagRow {
    Long getId();
    String getName();
    Boolean getIs_active();
    Long getCase_count();
    LocalDateTime getCreated_at();
}