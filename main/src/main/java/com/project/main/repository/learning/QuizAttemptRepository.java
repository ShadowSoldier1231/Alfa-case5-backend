package com.project.main.repository.learning;

import com.project.main.model.learning.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    @Query(value = """
        SELECT COUNT(*) AS attempts_count,
               COALESCE(MAX(CASE WHEN is_solved = true THEN 1 ELSE 0 END), 0) AS is_solved
        FROM quiz_attempt
        WHERE user_id = :userId AND quiz_id = :quizId
        """, nativeQuery = true)
    Object[] getQuizStatusByUserAndQuiz(@Param("userId") Long userId, @Param("quizId") Long quizId);
}