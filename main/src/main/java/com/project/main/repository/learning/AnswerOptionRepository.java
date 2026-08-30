package com.project.main.repository.learning;

import com.project.main.model.learning.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {

    @Query(value = "SELECT * FROM answer_option WHERE question_id = :questionId ORDER BY position ASC", nativeQuery = true)
    List<AnswerOption> findByQuestionId(@Param("questionId") Long questionId);

    @Query(value = "SELECT * FROM answer_option WHERE question_id IN (:questionIds) ORDER BY question_id ASC, position ASC", nativeQuery = true)
    List<AnswerOption> findByQuestionIdIn(@Param("questionIds") List<Long> questionIds);

    @Query(value = "SELECT COUNT(*) FROM answer_option WHERE question_id = :questionId AND is_correct = true", nativeQuery = true)
    long countCorrectByQuestionId(@Param("questionId") Long questionId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM answer_option WHERE question_id = :questionId", nativeQuery = true)
    void deleteByQuestionId(@Param("questionId") Long questionId);

    @Query(value = "SELECT id, is_correct FROM answer_option WHERE id IN (:ids)", nativeQuery = true)
    List<Object[]> findCorrectnessByIds(@Param("ids") List<Long> ids);
}