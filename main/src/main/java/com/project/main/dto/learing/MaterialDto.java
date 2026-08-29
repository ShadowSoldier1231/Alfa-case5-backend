package com.project.main.dto.learing;

import java.util.List;

public record MaterialDto(
        Long caseId,
        List<MaterialPart> materials
) {


    public record MaterialPart(Long id, String title, Integer position) {
    }
}
