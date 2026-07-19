package com.project.main.dto;

public class LeaderboardTopUser {
    private Long userId;
    private Long placement;
    private Long score;
    private String firstName;
    private String nickName;
    private String cityName;


    public LeaderboardTopUser(Long userId, Long placement, Long score,
                              String firstName, String nickName, String cityName) {
        this.userId = userId;
        this.placement = placement;
        this.score = score;
        this.firstName = firstName;
        this.nickName = nickName;
        this.cityName = cityName;
    }


    public Long getUserId() { return userId; }
    public Long getPlacement() { return placement; }
    public Long getScore() { return score; }
    public String getFirstName() { return firstName; }
    public String getNickName() { return nickName; }
    public String getCityName() { return cityName; }

    public void setPlacement(Long placement) {
        this.placement = placement;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

}
