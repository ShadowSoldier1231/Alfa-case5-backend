package com.project.main.dto.learing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AdminMaterialDto(
        Long caseId,
        List<AdminMaterialPart> materials
) {

    public record AdminMaterialPart(
            Long id,
            String title,
            Integer position,
            @JsonProperty("isActive") Boolean active
    ) {
    }
}