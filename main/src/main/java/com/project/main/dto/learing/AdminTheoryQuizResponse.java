package com.project.main.dto.learing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AdminTheoryQuizResponse(
        Long id,
        Long materialId,
        String title,
        @JsonProperty("isActive") Boolean active,
        List<AdminQuestionDto> questions
) {

    public record AdminQuestionDto(
            Long id,
            Long quizId,
            String text,
            Integer position,
            @JsonProperty("isActive") Boolean active,
            List<AdminOptionDto> options
    ) {
    }

    public record AdminOptionDto(
            Long id,
            Long questionId,
            String text,
            Integer position,
            @JsonProperty("isCorrect") Boolean correct
    ) {
    }
}