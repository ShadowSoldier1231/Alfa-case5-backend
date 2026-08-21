package com.project.main.dto.tags;

public record TagListItem(
        Long id,
        String name,
        Boolean active,
        Long caseCount
) {}