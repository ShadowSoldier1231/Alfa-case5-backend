package com.project.main.service.component;

import com.project.main.dto.auth.EmailVerificationEvent;
import com.project.main.dto.event.ForgotPasswordInitEvent;
import com.project.main.dto.event.ForgotUsernameEvent;
import com.project.main.repository.user.UserRepository;
import com.project.main.service.auth.VerificationRateLimitService;
import com.project.main.service.common.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
public class VerificationCodeListener {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final VerificationRateLimitService rateLimitService;

    public VerificationCodeListener(EmailService emailService,
                                    UserRepository userRepository,
                                    VerificationRateLimitService rateLimitService) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.rateLimitService = rateLimitService;
    }

    @Async("taskExecutor")
    @EventListener
    public void handleForgotUsername(ForgotUsernameEvent event) {
        userRepository.findByEmail(event.getEmail()).ifPresent(user -> {
            emailService.sendUsernameReminder(user.getEmail(), user.getUsername());
            rateLimitService.recordEmailSentByIp(event.getClientIp());
        });
    }

    @Async("taskExecutor")
    @EventListener
    public void handleForgotPasswordInit(ForgotPasswordInitEvent event) {
        emailService.sendPasswordResetCode(event.getEmail(), event.getCode());
        rateLimitService.recordEmailSentByIp(event.getClientIp());
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailVerification(EmailVerificationEvent event) {
        emailService.sendVerificationCode(event.getEmail(), event.getCode());
    }
}
