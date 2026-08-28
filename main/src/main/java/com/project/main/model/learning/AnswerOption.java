package com.project.main.model.learning;

import jakarta.persistence.*;

@Entity
@Table(name = "answer_option",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_answer_option_question_id_position", columnNames = {"question_id", "position"})
        },
        indexes = {
                @Index(name = "idx_answer_option_question_id", columnList = "question_id")
        })
public class AnswerOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    public AnswerOption() {
    }

    public AnswerOption(Long questionId, String text, Integer position, Boolean isCorrect) {
        this.questionId = questionId;
        this.text = text;
        this.position = position;
        this.isCorrect = isCorrect != null ? isCorrect : false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Boolean getCorrect() { return isCorrect; }
    public void setCorrect(Boolean correct) { isCorrect = correct; }
}