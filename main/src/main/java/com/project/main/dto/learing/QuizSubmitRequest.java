package com.project.main.dto.learing;

import java.util.List;

public class QuizSubmitRequest {
    private List<UserAnswerDto> answers;

    public List<UserAnswerDto> getAnswers() { return answers; }
    public void setAnswers(List<UserAnswerDto> answers) { this.answers = answers; }

    public static class UserAnswerDto {
        private Long questionId;
        private Long answerOptionId;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public Long getAnswerOptionId() { return answerOptionId; }
        public void setAnswerOptionId(Long answerOptionId) { this.answerOptionId = answerOptionId; }
    }
}