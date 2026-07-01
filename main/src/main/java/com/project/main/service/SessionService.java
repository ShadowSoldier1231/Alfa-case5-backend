package com.project.main.service;


import com.project.main.dto.RegisterResult;
import com.project.main.model.UserSession;
import com.project.main.repository.UserSessionRepository;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Component
public class SessionService {

    private final UserSessionRepository sessionRepository;

    public SessionService(UserSessionRepository sessionRepository){
        this.sessionRepository = sessionRepository;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void clearExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.deleteByExpiryDateBefore(now);
    }

    public Pair<RegisterResult, UserSession> checkCookie(String token){

        if (token == null) return Pair.of(new RegisterResult(false, "Please login first"), null);
        UserSession session = sessionRepository.findByToken(token)
                .orElse(null);
        if (session == null || session.getExpiryDate().isBefore(LocalDateTime.now())) {
            return Pair.of(new RegisterResult(false, "Session expired"), null);
        }
        return Pair.of(new RegisterResult(true, ""), session);
    }



}
