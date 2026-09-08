package com.project.main.repository.projection;

import java.time.LocalDate;

public interface PublicProfileRow {
    Long getId();
    String getFirst_name();
    String getLast_name();
    String getMiddle_name();
    LocalDate getBirthdate();
    String getStatus();
    String getNick_name();
    String getGender();
    Long getScore();
    Long getPlacement();
    String getCity_name();
    String getRegion_name();
    String getAvatar_url();
}