package com.project.main.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.model.Views;

public class WebUser{
private Long id;
private Long cityId;
@JsonView(Views.CityView.class)
private String cityName;
@JsonView(Views.CityView.class)
private String regionName;


public String getCityName() {
    return cityName;
}

public void setCityName(String cityName) {
    this.cityName = cityName;
}

public void setRegionName(String regionName) {
    this.regionName = regionName;
}

public String getRegionName() {
    return regionName;
}

public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public Long getCityId() {
    return cityId;
}

public void setCityId(Long cityId) {
    this.cityId = cityId;
}



}