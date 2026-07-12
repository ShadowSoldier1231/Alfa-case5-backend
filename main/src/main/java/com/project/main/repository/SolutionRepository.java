package com.project.main.repository;


import com.project.main.model.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, Long> {

    @Query(value = "SELECT SUM(max_rating) FROM (" +
            "  SELECT MAX(rating) as max_rating " +
            "  FROM solution " +
            "  WHERE user_id = :userId " +
            "  GROUP BY case_id" +
            ") as subquery",
            nativeQuery = true)
    Long getSumOfMaxRatingsByUserId(@Param("userId") Long userId);

    List<Solution> findByCaseIdAndUserIdOrderBySolutionIdAsc(Long caseId, Long userId);

}

