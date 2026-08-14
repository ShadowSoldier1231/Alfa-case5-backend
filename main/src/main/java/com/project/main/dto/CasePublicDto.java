package com.project.main.dto;

import com.project.main.enums.Difficulty;
import com.project.main.model.CaseEntity;

import java.time.LocalDateTime;
import java.util.List;

public record CasePublicDto(
        Long id, String slug, String title, String titleEn,
        String description, String fullDescription,
        Difficulty difficulty, Integer averageSolveMin,
        String pdfUrl, String iconUrl,
        Integer viewsCount, LocalDateTime createdAt, LocalDateTime updatedAt,
        List<TagInfo> tags) {

    public record TagInfo(Long id, String name, long count) {}

    public static CasePublicDto from(CaseEntity c, List<TagInfo> tags) {
        return new CasePublicDto(c.getId(), c.getSlug(), c.getTitle(), c.getTitleEn(),
                c.getDescription(), c.getFullDescription(), c.getDifficulty(),
                c.getAverageSolveMin(), c.getPdfUrl(), c.getIconUrl(),
                c.getViewsCount(), c.getCreatedAt(), c.getUpdatedAt(), tags);
    }
}