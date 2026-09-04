package com.project.main.controller.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class HealthController {

    private static final String UP = "UP";
    private static final String DOWN = "DOWN";
    private static final String UNKNOWN = "UNKNOWN";

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final DataSource dataSource;
    private final AtomicReference<String> cachedDbStatus = new AtomicReference<>(UNKNOWN);

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Scheduled(initialDelay = 0, fixedRate = 15000)
    public void checkDatabaseHealth() {
        try (var connection = dataSource.getConnection()) {
            cachedDbStatus.set(connection.isValid(2) ? UP : DOWN);
        } catch (Exception e) {
            log.warn("Database health check failed: {}", e.getMessage());
            cachedDbStatus.set(DOWN);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", UP,
                "timestamp", LocalDateTime.now().toString(),
                "database", cachedDbStatus.get()
        ));
    }

    @GetMapping("/health/live")
    public ResponseEntity<Map<String, Object>> live() {
        return ResponseEntity.ok(Map.of(
                "status", UP,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        String dbStatus = cachedDbStatus.get();
        boolean dbUp = UP.equals(dbStatus);

        Map<String, Object> body = Map.of(
                "status", dbUp ? UP : DOWN,
                "timestamp", LocalDateTime.now().toString(),
                "database", dbStatus
        );

        if (dbUp) {
            return ResponseEntity.ok(body);
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}