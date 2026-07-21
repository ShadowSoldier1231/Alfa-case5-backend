package com.project.main.repository;

import com.project.main.model.City;
import com.project.main.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Long> {

    @Query("SELECT d.firstName, d.lastName, d.middleName, d.birthdate, d.status, d.nickName, d.gender, " +
            "l.score, l.placement, c.cityName, c.regionName " +
            "FROM UserData d " +
            "JOIN LeaderboardUser l ON d.id = l.userId " +
            "LEFT JOIN City c ON d.cityId = c.id " +
            "WHERE d.id = :userId")
    Optional<Object[]> findProfileData(@Param("userId") Long userId);


    @Query("SELECT d.firstName, d.lastName, d.middleName, d.birthdate, d.status, d.nickName, d.gender, " +
            "l.score, l.placement, c.cityName, c.regionName, u.email " +
            "FROM UserData d " +
            "JOIN UserSetup u ON d.id = u.id " +
            "JOIN LeaderboardUser l ON d.id = l.userId " +
            "LEFT JOIN City c ON d.cityId = c.id " +
            "WHERE d.id = :userId")
    Optional<Object[]> findFullProfileData(@Param("userId") Long userId);
}