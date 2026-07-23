package com.project.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;


@RestController
public class HealthController {

    private final javax.sql.DataSource dataSource;

    public HealthController(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {

        String dbStatus;
        try (var connection = dataSource.getConnection()) {
            dbStatus = connection.isValid(2) ? "UP" : "DOWN";
        } catch (Exception e) {
            dbStatus = "DOWN: " + e.getMessage();
        }

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "database", dbStatus
        ));
    }
}
