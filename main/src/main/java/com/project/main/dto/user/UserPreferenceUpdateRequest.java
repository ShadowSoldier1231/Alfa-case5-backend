package com.project.main.dto.user;

import com.project.main.enums.Difficulty;

import java.util.List;

public class UserPreferenceUpdateRequest {
    private Difficulty preferredDifficulty;
    private List<Long> preferredTags;
    Boolean removeDifficulty;
    Boolean removeTags;

    public UserPreferenceUpdateRequest(){

    }
    public UserPreferenceUpdateRequest(Difficulty difficulty, List<Long> preferredTags, Boolean removeDifficulty, Boolean removeTags){
        this.preferredDifficulty  =difficulty;
        this.preferredTags = preferredTags;
        this.removeDifficulty = removeDifficulty;
        this.removeTags = removeTags;

    }

    public Boolean getRemoveDifficulty() {
        return removeDifficulty;
    }

    public Boolean getRemoveTags() {
        return removeTags;
    }

    public void setRemoveDifficulty(Boolean removeDifficulty) {
        this.removeDifficulty = removeDifficulty;
    }

    public void setRemoveTags(Boolean removeTags) {
        this.removeTags = removeTags;
    }

    public void setPreferredDifficulty(Difficulty preferredDifficulty) {
        this.preferredDifficulty = preferredDifficulty;
    }

    public Difficulty getPreferredDifficulty() {
        return preferredDifficulty;
    }

    public List<Long> getPreferredTags() {
        return preferredTags;
    }

    public void setPreferredTags(List<Long> preferredTags) {
        this.preferredTags = preferredTags;
    }
}
