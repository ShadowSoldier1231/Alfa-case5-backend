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

    @Query(value = "SELECT d.id, d.first_name, d.last_name, d.middle_name, d.birthdate, d.status, " +
            "d.nick_name, d.gender, l.score, l.placement, c.city_name, c.region_name " +
            "FROM user_data d " +
            "LEFT JOIN leaderboard_user l ON d.id = l.user_id " +
            "LEFT JOIN city c ON d.city_id = c.id " +
            "WHERE d.id = :userId", nativeQuery = true)
    Optional<Object[]> findProfileData(@Param("userId") Long userId);

    @Query(value = "SELECT d.id, d.first_name, d.last_name, d.middle_name, d.birthdate, d.status, " +
            "d.nick_name, d.gender, l.score, l.placement, c.city_name, c.region_name, u.email " +
            "FROM user_data d " +
            "JOIN user_setup u ON d.id = u.id " +
            "LEFT JOIN leaderboard_user l ON d.id = l.user_id " +
            "LEFT JOIN city c ON d.city_id = c.id " +
            "WHERE d.id = :userId", nativeQuery = true)
    Optional<Object[]> findFullProfileData(@Param("userId") Long userId);

    @Query(value = "SELECT c.* FROM user_data d " +
            "JOIN city c ON d.city_id = c.id " +
            "WHERE d.id = :userId", nativeQuery = true)
    Optional<City> findCityByUserId(@Param("userId") Long userId);
}