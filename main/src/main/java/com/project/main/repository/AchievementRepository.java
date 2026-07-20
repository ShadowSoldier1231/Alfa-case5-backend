package com.project.main.repository;


import com.project.main.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface AchievementRepository extends JpaRepository<UserAchievement, Long>{

    @Transactional
    void deleteAllByUserId(Long userId);
}