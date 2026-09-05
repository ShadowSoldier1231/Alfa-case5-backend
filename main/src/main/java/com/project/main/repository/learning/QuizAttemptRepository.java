package com.project.main.repository.learning;

import com.project.main.model.learning.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    @Query(value = """
    SELECT COUNT(*) AS attempts_count,
           COALESCE(MAX(CASE WHEN is_solved = true THEN 1 ELSE 0 END), 0) AS is_solved,
           COALESCE(MAX(score), 0) AS max_score
    FROM quiz_attempt
    WHERE user_id = :userId AND quiz_id = :quizId
    """, nativeQuery = true)
    List<Object[]> getQuizStatusByUserAndQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);

    @Query(
            value = "SELECT COUNT(*) FROM quiz_attempt WHERE quiz_id = :quizId",
            nativeQuery = true
    )
    long countByQuizId(@Param("quizId") Long quizId);

    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM quiz_attempt WHERE user_id = :userId",
            nativeQuery = true
    )
    void deleteByUserId(@Param("userId") Long userId);
}