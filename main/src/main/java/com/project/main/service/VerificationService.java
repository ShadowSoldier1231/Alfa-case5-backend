package com.project.main.service;


import com.project.main.dto.EmailVerificationEvent;
import com.project.main.enums.ValidationMethod;
import com.project.main.model.UserSetup;
import com.project.main.model.UserVerification;
import com.project.main.repository.UserRepository;
import com.project.main.repository.UserVerificationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class VerificationService {


    private final ApplicationEventPublisher eventPublisher;
    private final UserVerificationRepository verificationRepository;
    private final UserRepository userRepository;


    VerificationService(UserVerificationRepository verificationRepository, UserRepository userRepository,
                        ApplicationEventPublisher eventPublisher){
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public String generateVerification(Long userId, ValidationMethod method, String email) {
        UserVerification verification = new UserVerification();
        verification.setUserId(userId);


        String resultMessage = switch (method) {
            case TELEGRAM -> {
                String token = UUID.randomUUID().toString();
                verification.setTelegramVerificationToken(token);


                yield "https://t.me/alfa_auth_verification_bot?start=" + token;
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
    public String verifyUser(Long userId, Long verificationCode) {

        UserVerification verification = verificationRepository.findByUserIdAndEmailVerificationCode(
                userId,
                verificationCode
        ).orElse(null);

        if (verification == null) {
            return "Invalid or expired verification code";
        }

        UserSetup user = userRepository.findById(verification.getUserId()).orElse(null);
        if (user == null) {
            return "User does not exist";
        }

        user.setVerified(true);
        userRepository.save(user);

        verificationRepository.delete(verification);

        return "";
    }

}
