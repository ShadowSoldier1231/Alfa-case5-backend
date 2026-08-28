package com.project.main.repository.learning;

import com.project.main.model.learning.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    @Query(value = "SELECT * FROM user_answer WHERE user_id = :userId AND question_id = :questionId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<UserAnswer> findLatestByUserIdAndQuestionId(@Param("userId") Long userId, @Param("questionId") Long questionId);

    @Query(value = """
            SELECT DISTINCT ON (question_id) *
            FROM user_answer
            WHERE user_id = :userId AND question_id IN (:questionIds)
            ORDER BY question_id ASC, created_at DESC
            """, nativeQuery = true)
    List<UserAnswer> findLatestByUserIdAndQuestionIdIn(@Param("userId") Long userId, @Param("questionIds") List<Long> questionIds);

    @Query(value = "SELECT COUNT(*) FROM user_answer WHERE user_id = :userId AND question_id = :questionId", nativeQuery = true)
    long countByUserIdAndQuestionId(@Param("userId") Long userId, @Param("questionId") Long questionId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_answer WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_answer WHERE question_id = :questionId", nativeQuery = true)
    void deleteByQuestionId(@Param("questionId") Long questionId);
}