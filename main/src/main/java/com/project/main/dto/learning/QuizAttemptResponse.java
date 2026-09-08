package com.project.main.dto.learning;

public record QuizAttemptResponse(
        Long attemptId,
        Integer correctAnswers,
        Integer totalQuestions,
        Integer score,
        Boolean isSolved
) {}