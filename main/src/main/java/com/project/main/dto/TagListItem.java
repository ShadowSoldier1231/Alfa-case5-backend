package com.project.main.dto;

public record TagListItem(
        Long id,
        String name,
        Boolean active,
        Long caseCount
) {}