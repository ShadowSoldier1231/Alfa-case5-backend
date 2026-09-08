package com.project.main.dto.learning;

public record QuizStatusResponse(
        Long quizId,
        Integer attemptsCount,
        Boolean isSolved,
        Integer score
) {}