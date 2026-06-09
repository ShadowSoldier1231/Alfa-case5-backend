package com.project.main.controller;



import com.project.main.model.UserSetup;
import com.project.main.repository.CityRepository;
import com.project.main.repository.UserRepository;
import com.project.main.repository.UserSessionRepository;
import com.project.main.service.UserService;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/admin/v1")
public class AdminApiController {

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final UserSessionRepository sessionRepository;
    private  final UserService userService;

    public AdminApiController(UserRepository userRepository, CityRepository cityRepository, UserService userService, UserSessionRepository userSessionRepository) {
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.sessionRepository = userSessionRepository;
    }





    @GetMapping("/users")
    public ResponseEntity<Iterable<UserSetup>> getAllUsers() {

        Iterable<UserSetup> users = userRepository.findAll();

        return ResponseEntity.ok(users);
    }



}
