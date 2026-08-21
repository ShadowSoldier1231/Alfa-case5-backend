package com.project.main.repository.user;



import com.project.main.model.user.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_verification WHERE created_at < :threshold", nativeQuery = true)
    void deleteByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_verification WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Long userId);
}
