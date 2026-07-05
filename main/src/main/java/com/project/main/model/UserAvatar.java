package com.project.main.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;


@Entity
public class UserAvatar {

    @Id
    private Long userId;

    @Column(columnDefinition = "bytea")
    private byte[] pictureData;

    public UserAvatar() {}

    public UserAvatar(Long userId, byte[] pictureData) {
        this.userId = userId;
        this.pictureData = pictureData;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public byte[] getPictureData() {
        return pictureData;
    }
    public void setPictureData(byte[] pictureData) {
        this.pictureData = pictureData;
    }
}