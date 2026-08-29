package com.project.main.dto.learing;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminPartialMaterialDto(
        Long id,
        Long caseId,
        String title,
        Integer position,
        String text,
        @JsonProperty("isActive") Boolean active
) {
}