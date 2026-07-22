package com.project.main.repository;



import com.project.main.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
    Optional<UserVerification> findByUserIdAndEmailVerificationCode(Long userId, Long emailVerificationCode);

    @Query(value = "SELECT v.* FROM user_verification v " +
            "WHERE v.telegram_verification_token = :token " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM user_setup u " +
            "    WHERE u.telegram_id = :chatId AND u.id != v.user_id" +
            ")", nativeQuery = true)
    Optional<UserVerification> findByTokenAndTelegramIdAvailable(
            @Param("token") String token,
            @Param("chatId") Long chatId
    );

    @Transactional
    void deleteByUserId(Long userId);
}
