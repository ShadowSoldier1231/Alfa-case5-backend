package com.project.main.model.learning;

import jakarta.persistence.*;

@Entity
@Table(name = "question",
        uniqueConstraints = @UniqueConstraint(name = "uk_question_quiz_id_position", columnNames = {"quiz_id", "position"}),
        indexes = @Index(name = "idx_question_quiz_id", columnList = "quiz_id")
)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Question() {
    }

    public Question(Long testId, String text, Integer position) {
        this.quizId = testId;
        this.text = text;
        this.position = position;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Boolean getActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }
}