package com.project.main.service;


import com.project.main.dto.RegisterResult;
import com.project.main.dto.UserDeletedEvent;
import com.project.main.model.UserSession;
import com.project.main.repository.UserSessionRepository;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;


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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {
        sessionRepository.deleteByUserId(event.userId());
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

    public ResponseCookie generateCookie(){
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String secureValue = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        return ResponseCookie.from("token", secureValue)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7200)
                .sameSite("Lax")
                .build();
    }

    public void createSession(String value, Long userId){
        UserSession session = new UserSession();
        session.setToken(value);
        session.setUserId(userId);
        session.setExpiryDate(LocalDateTime.now().plusHours(2));
        sessionRepository.save(session);
    }

    @Transactional
    public ResponseCookie deleteCookie(String token, boolean deleteToken){

        if(deleteToken){
            sessionRepository.deleteByToken(token);
        }


        return ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie deleteCookie(String token){

        return deleteCookie(token, true);
    }

    public void deleteAllSessions(Long userId){
        sessionRepository.deleteAllByUserId(userId);
    }




}
