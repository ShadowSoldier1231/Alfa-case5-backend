package com.project.main.repository.projection;

public interface LeaderboardTopRow {
    Long getUser_id();
    Long getScore();
    String getFirst_name();
    String getNick_name();
    String getCity_name();
    String getAvatar_url();
}