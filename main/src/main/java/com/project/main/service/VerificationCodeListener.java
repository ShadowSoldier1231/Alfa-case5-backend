package com.project.main.service;

import com.project.main.dto.EmailVerificationEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
public class VerificationCodeListener {

    private final EmailService emailService;

    public VerificationCodeListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailVerification(EmailVerificationEvent event) {
        emailService.sendVerificationCode(event.getEmail(), event.getCode());
    }
}
