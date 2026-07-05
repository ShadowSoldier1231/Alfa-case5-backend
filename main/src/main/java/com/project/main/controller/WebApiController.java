package com.project.main.controller;


import com.fasterxml.jackson.annotation.JsonView;
import com.project.main.dto.WebUser;
import com.project.main.model.City;
import com.project.main.model.Views;
import com.project.main.repository.CityRepository;
import com.project.main.service.FetchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/site")
public class WebApiController {

    private final FetchingService fetchingService;
    private final CityRepository cityRepository;

    public WebApiController(CityRepository cityRepository, FetchingService fetchingService) {

        this.cityRepository = cityRepository;
        this.fetchingService = fetchingService;
    }



    @JsonView(Views.CityView.class)
    @GetMapping("/user/{id}/city")
    public ResponseEntity<WebUser>  getCityName(@PathVariable long id){
        return ResponseEntity.ok(fetchingService.getCityByUserId(id));

    }


    @GetMapping("/searchLocation/{cityName}")
    public ResponseEntity<List<City>> getCityId(@PathVariable String cityName){
        return ResponseEntity.ok(cityRepository.findByCityNameContainingIgnoreCase(cityName));

    }

    @GetMapping("/getAllCities")
    public ResponseEntity<List<City>> getAllCities(){
        return ResponseEntity.ok(cityRepository.findAll());

    }


}




