package com.project.main.repository.user;

import com.project.main.model.common.City;
import com.project.main.model.user.UserData;
import com.project.main.repository.projection.MyProfileRow;
import com.project.main.repository.projection.PublicProfileRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Long> {

    @Query(
            value = """
                SELECT
                    d.id AS id,
                    d.first_name AS first_name,
                    d.last_name AS last_name,
                    d.middle_name AS middle_name,
                    d.birthdate AS birthdate,
                    d.status AS status,
                    d.nick_name AS nick_name,
                    d.gender AS gender,
                    COALESCE(l.score, 0) AS score,
                    (
                        SELECT COUNT(*) + 1
                        FROM leaderboard_user l2
                        JOIN user_setup us2 ON l2.user_id = us2.id
                        WHERE us2.is_verified = true
                          AND (
                              l2.score > COALESCE(l.score, 0)
                              OR (
                                  l2.score = COALESCE(l.score, 0)
                                  AND l2.user_id < d.id
                              )
                          )
                    ) AS placement,
                    c.city_name AS city_name,
                    c.region_name AS region_name,
                    d.avatar_url AS avatar_url
                FROM user_data d
                LEFT JOIN leaderboard_user l ON d.id = l.user_id
                LEFT JOIN city c ON d.city_id = c.id
                WHERE d.id = :userId
                """,
            nativeQuery = true
    )
    Optional<PublicProfileRow> findProfileData(@Param("userId") Long userId);

    @Query(
            value = """
                SELECT
                    d.id AS id,
                    d.first_name AS first_name,
                    d.last_name AS last_name,
                    d.middle_name AS middle_name,
                    d.birthdate AS birthdate,
                    d.status AS status,
                    d.nick_name AS nick_name,
                    d.gender AS gender,
                    COALESCE(l.score, 0) AS score,
                    (
                        SELECT COUNT(*) + 1
                        FROM leaderboard_user l2
                        JOIN user_setup us2 ON l2.user_id = us2.id
                        WHERE us2.is_verified = true
                          AND (
                              l2.score > COALESCE(l.score, 0)
                              OR (
                                  l2.score = COALESCE(l.score, 0)
                                  AND l2.user_id < d.id
                              )
                          )
                    ) AS placement,
                    c.city_name AS city_name,
                    c.region_name AS region_name,
                    u.email AS email,
                    d.avatar_url AS avatar_url
                FROM user_data d
                JOIN user_setup u ON d.id = u.id
                LEFT JOIN leaderboard_user l ON d.id = l.user_id
                LEFT JOIN city c ON d.city_id = c.id
                WHERE d.id = :userId
                """,
            nativeQuery = true
    )
    Optional<MyProfileRow> findFullProfileData(@Param("userId") Long userId);

    @Query(value = "SELECT c.* FROM user_data d " +
            "JOIN city c ON d.city_id = c.id " +
            "WHERE d.id = :userId", nativeQuery = true)
    Optional<City> findCityByUserId(@Param("userId") Long userId);
}