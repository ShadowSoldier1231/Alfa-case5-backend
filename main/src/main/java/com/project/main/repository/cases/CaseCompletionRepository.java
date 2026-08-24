package com.project.main.repository.cases;

import com.project.main.model.cases.CaseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CaseCompletionRepository extends JpaRepository<CaseCompletion, Long> {

    @Query(
            value = """
                    SELECT EXISTS(
                        SELECT 1
                        FROM case_completions
                        WHERE user_id = :userId
                          AND case_id = :caseId
                    )
                    """,
            nativeQuery = true
    )
    boolean existsByUserIdAndCaseId(
            @Param("userId") Long userId,
            @Param("caseId") Long caseId
    );


    @Modifying
    @Transactional
    @Query(
            value = """
                    DELETE FROM case_completions
                    WHERE user_id = :userId
                    """,
            nativeQuery = true
    )
    void deleteByUserId(@Param("userId") Long userId);
}