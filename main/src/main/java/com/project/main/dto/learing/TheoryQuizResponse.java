package com.project.main.dto.learing;

import java.util.List;

public record TheoryQuizResponse(
        Long id,
        List<QuestionDto> questions
) {

    public record QuestionDto(
            Long id,
            Integer position,
            String text,
            List<OptionDto> options
    ) {
    }

    public record OptionDto(
            Long id,
            Integer position,
            String text
    ) {
    }
}