package com.project.main.dto.integration;

import com.project.main.model.common.Solution;

public record ChatMessageDto(
        Long solutionId,
        Long rating,
        String solutionText,
        String solutionResponse
) {
    public static ChatMessageDto from(Solution solution) {
        return new ChatMessageDto(
                solution.getSolutionId(),
                solution.getRating(),
                solution.getSolutionText(),
                solution.getSolutionResponse()
        );
    }
}
