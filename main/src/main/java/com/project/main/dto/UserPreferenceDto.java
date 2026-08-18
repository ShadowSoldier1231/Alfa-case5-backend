package com.project.main.dto;

import com.project.main.enums.Difficulty;

import java.util.List;

public class UserPreferenceDto {

    private Long userId;
    private Difficulty preferredDifficulty;
    private List<TagListItem> preferredTags;

    public UserPreferenceDto(){

    }
    public UserPreferenceDto(Difficulty difficulty, List<TagListItem> preferredTags, Long userId){
        this.preferredDifficulty  =difficulty;
        this.preferredTags = preferredTags;
        this.userId = userId;

    }

    public void setPreferredTags(List<TagListItem> preferredTags) {
        this.preferredTags = preferredTags;
    }

    public List<TagListItem> getPreferredTags() {
        return preferredTags;
    }

    public Difficulty getPreferredDifficulty() {
        return preferredDifficulty;
    }

    public void setPreferredDifficulty(Difficulty preferredDifficulty) {
        this.preferredDifficulty = preferredDifficulty;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
