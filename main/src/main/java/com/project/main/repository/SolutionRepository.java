package com.project.main.repository;


import com.project.main.model.Solution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    void deleteAllByUserId( Long userId);

    @Query(value = "SELECT COALESCE(MAX(rating), 0) FROM solution WHERE case_id = :caseId AND user_id = :userId", nativeQuery = true)
    Long getMaxRatingByCaseIdAndUserId(@Param("caseId") Long caseId, @Param("userId") Long userId);


    @Query(
            value = """
        SELECT *
        FROM solution
        WHERE case_id = :caseId
          AND user_id = :userId
        """,
            countQuery = """
        SELECT COUNT(solution_id)
        FROM solution
        WHERE case_id = :caseId
          AND user_id = :userId
        """,
            nativeQuery = true
    )
    Page<Solution> findChatSequence(
            @Param("caseId") Long caseId,
            @Param("userId") Long userId,
            Pageable pageable
    );

}

