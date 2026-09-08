package com.project.main.repository.projection;

import java.time.LocalDateTime;

public interface AdminUserRow {
    Long getId();
    String getUsername();
    String getEmail();
    String getNick_name();
    String getRole();
    String getStatus();
    Boolean getIs_verified();
    LocalDateTime getBanned_until();
}