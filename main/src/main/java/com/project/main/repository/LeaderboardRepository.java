package com.project.main.repository;

import com.project.main.model.LeaderboardUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardUser, Long> {

    @Modifying
    @Query(value = "UPDATE leaderboard_user SET score = :score WHERE user_id = :userId", nativeQuery = true)
    void updateScore(@Param("userId") Long userId, @Param("score") Long score);

    @Query(value = "SELECT l.user_id, l.score, u.first_name, u.nick_name, c.city_name, u.avatar_url " +
            "FROM leaderboard_user l " +
            "JOIN user_data u ON l.user_id = u.id " +
            "JOIN user_setup us ON l.user_id = us.id " +
            "LEFT JOIN city c ON u.city_id = c.id " +
            "WHERE us.is_verified = true " +
            "ORDER BY l.score DESC, l.user_id ASC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5LeaderboardData();

    List<LeaderboardUser> findTop5ByOrderByScoreDescUserIdAsc();


    @Modifying
    @Query(value = "UPDATE leaderboard_user SET score = (SELECT COALESCE(SUM(max_rating), 0) FROM (SELECT MAX(rating) as max_rating FROM solution WHERE user_id = :userId GROUP BY case_id) as sub) WHERE user_id = :userId", nativeQuery = true)
    void recalculateAndSetScore(@Param("userId") Long userId);

    @Query(value = "SELECT l.user_id, MAX(s.rating) as case_score, u.first_name, u.nick_name, c.city_name, u.avatar_url " +
            "FROM leaderboard_user l " +
            "JOIN solution s ON l.user_id = s.user_id AND s.case_id = :caseId " +
            "JOIN user_data u ON l.user_id = u.id " +
            "JOIN user_setup us ON l.user_id = us.id " +
            "LEFT JOIN city c ON u.city_id = c.id " +
            "WHERE us.is_verified = true " +
            "GROUP BY l.user_id, u.first_name, u.nick_name, c.city_name, u.avatar_url " +
            "ORDER BY case_score DESC, l.user_id ASC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5LeaderboardDataByCaseId(@Param("caseId") Long caseId);


    @Query(value = "SELECT COUNT(*) + 1 FROM (" +
            "SELECT l.user_id, MAX(s.rating) as case_score " +
            "FROM leaderboard_user l " +
            "JOIN solution s ON l.user_id = s.user_id AND s.case_id = :caseId " +
            "JOIN user_setup us ON l.user_id = us.id " +
            "WHERE us.is_verified = true " +
            "GROUP BY l.user_id " +
            "HAVING MAX(s.rating) > (SELECT COALESCE(MAX(rating), 0) FROM solution WHERE case_id = :caseId AND user_id = :userId)" +
            ") as better_users", nativeQuery = true)
    Long getUserPlacementInCase(@Param("caseId") Long caseId, @Param("userId") Long userId);

    @Query(value = "SELECT rank FROM (" +
            "SELECT l.user_id, ROW_NUMBER() OVER (ORDER BY l.score DESC, l.user_id ASC) as rank " +
            "FROM leaderboard_user l " +
            "JOIN user_setup us ON l.user_id = us.id " +
            "WHERE us.is_verified = true" +
            ") t WHERE t.user_id = :userId", nativeQuery = true)
    Long getGlobalUserPlacement(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) FROM leaderboard_user l " +
            "JOIN user_setup us ON l.user_id = us.id " +
            "WHERE us.is_verified = true AND l.score > 0", nativeQuery = true)
    Long getTotalVerifiedUsersInLeaderboard();
}