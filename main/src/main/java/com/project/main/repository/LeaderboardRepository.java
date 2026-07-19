package com.project.main.repository;

import com.project.main.model.LeaderboardUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardUser, Long> {


    List<LeaderboardUser> findTop5ByOrderByScoreDescUserIdAsc();


    @Modifying
    @Transactional
    @Query(value = "UPDATE leaderboard_user u " +
            "SET placement = ( " +
            "    SELECT pos " +
            "    FROM ( " +
            "        SELECT user_id, " +
            "        ROW_NUMBER() OVER (ORDER BY score DESC, user_id ASC) as pos " +
            "        FROM leaderboard_user " +
            "    ) temp " +
            "    WHERE temp.user_id = u.user_id " +
            ")", nativeQuery = true)
    void updateAllPlacements();
}
