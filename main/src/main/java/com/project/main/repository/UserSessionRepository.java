package com.project.main.repository;

import com.project.main.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


public interface UserSessionRepository extends JpaRepository<UserSession, String> {


    Optional<UserSession> findByToken(String token);

    @Modifying
    @Transactional
    void deleteByExpiryDateBefore(LocalDateTime now);

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);

    @Modifying
    @Transactional
    void deleteByToken(String token);
}