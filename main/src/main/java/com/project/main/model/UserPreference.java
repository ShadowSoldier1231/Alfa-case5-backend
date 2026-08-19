package com.project.main.model;


import com.project.main.enums.Difficulty;
import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "user_preferences")
public class UserPreference {
    @Id
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Difficulty preferredDifficulty;

    @ElementCollection
    @CollectionTable(
            name = "user_preferred_tags",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "preferred_tags")
    private List<Long> preferredTagIds;


    public UserPreference(){

    }
    public UserPreference(Long userId, Difficulty difficulty, List<Long> preferredTagIds){
        this.userId = userId;
        this.preferredTagIds = preferredTagIds;
        this.preferredDifficulty = difficulty;
    }


    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public Difficulty getPreferredDifficulty() {
        return preferredDifficulty;
    }

    public List<Long> getPreferredTagIds() {
        return preferredTagIds;
    }

    public void setPreferredDifficulty(Difficulty preferredDifficulty) {
        this.preferredDifficulty = preferredDifficulty;
    }

    public void setPreferredTagIds(List<Long> preferredTagIds) {
        this.preferredTagIds = preferredTagIds;
    }

}
