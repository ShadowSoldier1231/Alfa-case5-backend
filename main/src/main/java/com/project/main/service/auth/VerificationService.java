package com.project.main.service.auth;


import com.project.main.dto.auth.EmailVerificationEvent;
import com.project.main.enums.ValidationMethod;
import com.project.main.exception.*;
import com.project.main.model.user.UserSetup;
import com.project.main.model.user.UserVerification;
import com.project.main.repository.user.UserRepository;
import com.project.main.repository.user.UserVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class VerificationService {


    private final String botName;
    private final ApplicationEventPublisher eventPublisher;
    private final UserVerificationRepository verificationRepository;
    private final UserRepository userRepository;


    VerificationService(UserVerificationRepository verificationRepository, UserRepository userRepository,
                        ApplicationEventPublisher eventPublisher,
                        @Value("${telegram.bot.username}") String botName){
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.botName = botName;
    }

    @Transactional
    public String generateVerification(Long userId, ValidationMethod method, String email) {
        verificationRepository.deleteByUserId(userId);

        UserVerification verification = new UserVerification();
        verification.setUserId(userId);


        String resultMessage = switch (method) {
            case TELEGRAM -> {
                String token = UUID.randomUUID().toString();
                verification.setTelegramVerificationToken(token);


                yield "https://t.me/" + botName + "?start=" + token;
            }
            case EMAIL -> {
                long code = ThreadLocalRandom.current().nextLong(100_000, 1_000_000);
                verification.setEmailVerificationCode(code);
                eventPublisher.publishEvent(new EmailVerificationEvent(email, code));

                yield "Verification code sent to your email";
            }
        };


        verificationRepository.save(verification);

        return resultMessage;
    }


    @Transactional
    public boolean verifyTelegramUser(String token, Long chatId) {
        return verificationRepository.findByTokenAndTelegramIdAvailable(token, chatId)
                .map(verification -> {
                    return userRepository.findById(verification.getUserId())
                            .map(user -> {
                                user.setTelegramId(chatId);
                                user.setVerified(true);
                                verificationRepository.deleteByUserId(verification.getUserId());
                                return true;
                            })
                            .orElse(false);
                })
                .orElse(false);
    }

    @Transactional
    public void verifyUser(Long userId, Long verificationCode) {

        UserVerification verification = verificationRepository.findByUserIdAndEmailVerificationCode(
                userId,
                verificationCode
        ).orElseThrow(() -> new BadRequestException("Invalid or expired verification code"));

        UserSetup user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new NotFoundException("User does not exist"));
        if (verification.getCreatedAt() == null || verification.getCreatedAt().isBefore(LocalDateTime.now().minusHours(1))) {
            verificationRepository.delete(verification);
            throw new BadRequestException("Invalid or expired verification code");
        }
        user.setVerified(true);
        userRepository.save(user);

        verificationRepository.delete(verification);
    }

    @Transactional
    public long createPasswordResetCode(Long userId) {
        verificationRepository.deleteByUserId(userId);

        long code = ThreadLocalRandom.current().nextLong(100_000, 1_000_000);

        UserVerification verification = new UserVerification(userId, code);
        verificationRepository.save(verification);

        return code;
    }

    @Transactional
    public void validateAndConsumeResetCode(Long userId, Long code) {
        UserVerification verification = verificationRepository.findByUserIdAndEmailVerificationCode(userId, code)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification code"));

        if (verification.getCreatedAt() == null || verification.getCreatedAt().isBefore(LocalDateTime.now().minusHours(1))) {
            verificationRepository.delete(verification);
            throw new BadRequestException("Invalid or expired verification code");
        }

        verificationRepository.delete(verification);
    }




    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void clearExpiredVerifications() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        verificationRepository.deleteByCreatedAtBefore(threshold);
    }
}
