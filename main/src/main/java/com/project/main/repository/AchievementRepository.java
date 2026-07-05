package com.project.main.repository;


import com.project.main.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AchievementRepository extends JpaRepository<UserAchievement, Long>{

}