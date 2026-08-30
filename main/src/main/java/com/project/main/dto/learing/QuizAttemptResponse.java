package com.project.main.dto.learing;

public record QuizAttemptResponse(
        Long attemptId,
        Integer correctAnswers,
        Integer totalQuestions,
        Integer score,
        Boolean isSolved
) {}