package com.project.main.repository.cases;

import com.project.main.model.cases.CaseRating;
import com.project.main.repository.projection.CaseAverageRatingRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRatingRepository extends JpaRepository<CaseRating, Long> {

    Optional<CaseRating> findByUserIdAndCaseId(Long userId, Long caseId);

    @Query(
            value = """
                SELECT
                    case_id AS case_id,
                    CAST(AVG(rating) AS double precision) AS avg_rating
                FROM case_rating
                WHERE case_id IN (:caseIds)
                GROUP BY case_id
                """,
            nativeQuery = true
    )
    List<CaseAverageRatingRow> findAverageRatingsByCaseIds(@Param("caseIds") List<Long> caseIds);

    @Transactional
    void deleteByUserId(Long userId);
}