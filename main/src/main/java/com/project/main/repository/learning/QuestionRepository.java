package com.project.main.repository.learning;


import com.project.main.model.learning.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    @Query(value = "SELECT * FROM question WHERE quiz_id = :quizId AND is_active = true ORDER BY position ASC", nativeQuery = true)
    List<Question> findActiveByQuizId(@Param("quizId") Long quizId);

    @Query(value = "SELECT * FROM question WHERE quiz_id = :quizId ORDER BY position ASC", nativeQuery = true)
    List<Question> findAllByQuizId(@Param("quizId") Long quizId);

    @Query(value = "SELECT COALESCE(MAX(position), 0) FROM question WHERE quiz_id = :quizId", nativeQuery = true)
    int getMaxPositionByQuizId(@Param("quizId") Long quizId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM question WHERE quiz_id = :quizId", nativeQuery = true)
    void deleteByQuizId(@Param("quizId") Long quizId);
}