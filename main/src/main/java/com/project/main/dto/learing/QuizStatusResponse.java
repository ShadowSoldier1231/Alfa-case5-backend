package com.project.main.dto.learing;

public record QuizStatusResponse(
        Long quizId,
        Integer attemptsCount,
        Boolean isSolved
) {}