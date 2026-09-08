package com.project.main.repository.projection;

import java.time.LocalDateTime;

public interface FavoriteCaseRow {
    Long getId();
    String getSlug();
    String getTitle();
    String getTitle_en();
    String getDescription();
    String getFull_description();
    String getDifficulty();
    Integer getAverage_solve_min();
    String getPdf_url();
    String getIcon_url();
    Integer getViews_count();
    LocalDateTime getCreated_at();
    LocalDateTime getUpdated_at();
    LocalDateTime getAdded_at();
}