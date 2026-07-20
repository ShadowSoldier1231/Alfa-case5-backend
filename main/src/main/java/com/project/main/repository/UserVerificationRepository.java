package com.project.main.repository;



import com.project.main.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
    Optional<UserVerification> findByUserIdAndEmailVerificationCode(Long userId, Long emailVerificationCode);
    Optional<UserVerification> findByTelegramVerificationToken(String token);

    @Transactional
    void deleteByUserId(Long userId);
}
