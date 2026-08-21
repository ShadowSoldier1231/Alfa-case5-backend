package com.project.main.repository.user;


import com.project.main.model.user.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface AchievementRepository extends JpaRepository<UserAchievement, Long>{

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_achievement WHERE user_id = :userId", nativeQuery = true)
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM user_achievement WHERE user_id = :userId AND achievement_id = :achievementId)", nativeQuery = true)
    boolean existsByUserIdAndAchievementId(@Param("userId") Long userId, @Param("achievementId") Long achievementId);

    @Query(value = "SELECT achievement_id, obtained_at FROM user_achievement WHERE user_id = :userId", nativeQuery = true)
    List<Object[]> findObtainedAchievementsByUserId(@Param("userId") Long userId);

}