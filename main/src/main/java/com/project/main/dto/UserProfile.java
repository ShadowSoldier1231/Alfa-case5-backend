package com.project.main.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserStatus;
import com.project.main.model.City;
import com.project.main.model.LeaderboardUser;
import com.project.main.model.UserData;
import com.project.main.model.Views;


import java.time.LocalDate;


public class UserProfile {
    @JsonView(Views.PublicProfile.class)
    private Long id;
    @JsonView(Views.PublicProfile.class)
    private String cityName;
    @JsonView(Views.PublicProfile.class)
    private String regionName;
    @JsonView(Views.PublicProfile.class)
    private UserStatus status;
    @JsonView(Views.PublicProfile.class)
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthdate;
    @JsonView(Views.PublicProfile.class)
    private  String firstName;
    @JsonView(Views.PublicProfile.class)
    private String middleName;
    private String lastName;
    @JsonView(Views.PublicProfile.class)
    private String nickName;
    @JsonView(Views.PublicProfile.class)
    private GenderCode gender;
    @JsonView(Views.PublicProfile.class)
    private Long score;
    @JsonView(Views.PublicProfile.class)
    private Long placement;
    @JsonView(Views.MyProfile.class)
    private String email;

    public UserProfile() {
    }

    private UserProfile(Builder builder) {
        this.id = builder.id;
        this.cityName = builder.cityName;
        this.regionName = builder.regionName;
        this.status = builder.status;
        this.birthdate = builder.birthdate;
        this.firstName = builder.firstName;
        this.middleName = builder.middleName;
        this.lastName = builder.lastName;
        this.nickName = builder.nickName;
        this.gender = builder.gender;
        this.score = builder.score;
        this.placement = builder.placement;
        this.email = builder.email;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private Long id;
        private String cityName;
        private String regionName;
        private UserStatus status;
        @JsonFormat(pattern = "dd.MM.yyyy")
        private LocalDate birthdate;
        private String firstName;
        private String middleName;
        private String lastName;
        private String nickName;
        private GenderCode gender;
        private Long score;
        private Long placement;
        private String email;



        public Builder include(UserData userData) {
            if (userData != null) {
                this.id = userData.getId();
                this.status = userData.getStatus();
                this.birthdate = userData.getBirthdate();
                this.firstName = userData.getFirstName();
                this.middleName = userData.getMiddleName();
                this.lastName = userData.getLastName();
                this.nickName = userData.getNickName();
                this.gender = userData.getGender();
            }
            return this;
        }
        public Builder include(City city) {
            if (city != null) {
                this.cityName = city.getCityName();
                this.regionName = city.getRegionName();
            }
            return this;
        }

        public Builder include(LeaderboardUser leaderboardUser) {
            if (leaderboardUser != null) {
                this.score = leaderboardUser.getScore();
                this.placement = leaderboardUser.getPlacement();
            }
            return this;
        }


        public Builder cityName(String cityName) {
            this.cityName = cityName;
            return this;
        }

        public Builder regionName(String regionName) {
            this.regionName = regionName;
            return this;
        }

        public Builder id(Long id){
            this.id = id;
            return this;
        }

        public Builder email(String email){
            this.email = email;
            return this;
        }
        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public Builder birthdate(LocalDate birthdate) {
            this.birthdate = birthdate;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder nickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public Builder gender(GenderCode gender) {
            this.gender = gender;
            return this;
        }

        public Builder score(Long score) {
            this.score = score;
            return this;
        }

        public Builder placement(Long placement) {
            this.placement = placement;
            return this;
        }



        public UserProfile build() {
            return new UserProfile(this);
        }
    }


    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setGender(GenderCode gender) {
        this.gender = gender;
    }

    public GenderCode getGender() {
        return gender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public Long getPlacement() {
        return placement;
    }

    public Long getScore() {
        return score;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getLastName() {
        return lastName;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }



    public void setPlacement(Long placement) {
        this.placement = placement;
    }

    public void setScore(Long score) {
        this.score = score;
    }

}