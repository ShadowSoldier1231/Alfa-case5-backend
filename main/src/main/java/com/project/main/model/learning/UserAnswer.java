package com.project.main.model.learning;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_answer", indexes = {
        @Index(name = "idx_user_answer_user_id", columnList = "user_id"),
        @Index(name = "idx_user_answer_question_id", columnList = "question_id")
})
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "answer_option_id", nullable = false)
    private Long answerOptionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserAnswer() {
    }

    public UserAnswer(Long userId, Long questionId, Long answerOptionId) {
        this.userId = userId;
        this.questionId = questionId;
        this.answerOptionId = answerOptionId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Long getAnswerOptionId() { return answerOptionId; }
    public void setAnswerOptionId(Long answerOptionId) { this.answerOptionId = answerOptionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}