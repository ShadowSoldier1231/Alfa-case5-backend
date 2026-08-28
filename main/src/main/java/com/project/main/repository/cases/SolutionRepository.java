package com.project.main.repository.cases;


import com.project.main.model.cases.Solution;
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
        SELECT COUNT(*)
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

    @Query(
            value = """
        SELECT *
        FROM solution
        WHERE user_id = :userId
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM solution
        WHERE user_id = :userId
        """,
            nativeQuery = true
    )
    Page<Solution> getAllSolutionsForUser(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query(value = "SELECT DISTINCT DATE(created_at) as solve_date " +
            "FROM solution " +
            "WHERE user_id = :userId AND rating >= :threshold " +
            "ORDER BY solve_date DESC", nativeQuery = true)
    List<Object[]> findDistinctSolveDatesByUserId(@Param("userId") Long userId, @Param("threshold") Long threshold);


    @Query(value = "SELECT EXISTS(" +
            "    SELECT 1 FROM solution s " +
            "    JOIN cases c ON s.case_id = c.id " +
            "    WHERE s.user_id = :userId " +
            "    AND s.solved_min IS NOT NULL AND c.average_solve_min IS NOT NULL " +
            "    AND s.solved_min < c.average_solve_min AND s.rating >= :threshold" +
            ")", nativeQuery = true)
    boolean existsFasterThanAverageSolution(@Param("userId") Long userId, @Param("threshold") Long threshold);



    @Query(value = "SELECT EXISTS(" +
            "    SELECT 1 FROM solution " +
            "    WHERE solution_id = (" +
            "        SELECT MIN(solution_id) FROM solution " +
            "        WHERE user_id = :userId AND rating >= :threshold" +
            "    ) " +
            "    AND solved_min IS NOT NULL AND solved_min < 30" +
            ")", nativeQuery = true)
    boolean existsFirstSolutionUnder30Min(@Param("userId") Long userId, @Param("threshold") Long threshold);


    @Query(value = "SELECT COUNT(DISTINCT case_id) FROM solution WHERE user_id = :userId AND rating >= :threshold", nativeQuery = true)
    Long countDistinctCasesSolvedByUserId(@Param("userId") Long userId, @Param("threshold") Long threshold);

    @Query(value = "SELECT COUNT(DISTINCT s.case_id) FROM solution s " +
            "JOIN cases c ON s.case_id = c.id " +
            "WHERE s.user_id = :userId AND c.difficulty = 'HARD' AND s.rating >= :threshold", nativeQuery = true)
    Long countDistinctHardCasesSolvedByUserId(@Param("userId") Long userId, @Param("threshold") Long threshold);



    @Query(value = "SELECT COUNT(*) FROM (" +
            "  SELECT MAX(rating) as max_r " +
            "  FROM solution " +
            "  WHERE user_id = :userId " +
            "  GROUP BY case_id " +
            "  HAVING MAX(rating) = 100" +
            ") as sub", nativeQuery = true)
    Long countCasesWithMaxRatingByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(DISTINCT ct.tag_id) " +
            "FROM solution s " +
            "INNER JOIN case_tags ct ON s.case_id = ct.case_id " +
            "WHERE s.user_id = :userId AND s.rating >= :threshold", nativeQuery = true)
    Long countUniqueTagsInSolvedCases(@Param("userId") Long userId, @Param("threshold") Long threshold);


    @Query(value = "SELECT COUNT(DISTINCT s.case_id) " +
            "FROM solution s " +
            "INNER JOIN case_tags ct ON s.case_id = ct.case_id " +
            "INNER JOIN tags t ON ct.tag_id = t.id " +
            "WHERE s.user_id = :userId AND t.name = :tagName AND s.rating >= :threshold", nativeQuery = true)
    Long countSolvedCasesByTagName(@Param("userId") Long userId, @Param("tagName") String tagName, @Param("threshold") Long threshold);


    @Query(value = "SELECT COUNT(*) = 1 FROM solution WHERE user_id = :userId AND case_id = :caseId", nativeQuery = true)
    boolean isFirstSolutionForCase(@Param("userId") Long userId, @Param("caseId") Long caseId);

    @Query(
            value = """
        SELECT *
        FROM solution
        WHERE case_id = :caseId
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM solution
        WHERE case_id = :caseId
        """,
            nativeQuery = true
    )
    Page<Solution> getAllSolutionsForCase(
            @Param("caseId") Long caseId,
            Pageable pageable
    );
}

