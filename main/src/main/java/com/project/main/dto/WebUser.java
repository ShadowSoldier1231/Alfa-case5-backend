package com.project.main.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.model.Views;

public class WebUser{

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


}