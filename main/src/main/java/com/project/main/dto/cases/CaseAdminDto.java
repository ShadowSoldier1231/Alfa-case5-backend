package com.project.main.dto.cases;


import com.project.main.enums.Difficulty;
import com.project.main.model.cases.CaseEntity;

import java.time.LocalDateTime;
import java.util.List;

public record CaseAdminDto(
        Long id, String slug, String title, String titleEn,
        String description, String fullDescription,
        Difficulty difficulty, Integer averageSolveMin,
        String pdfUrl, String iconUrl,
        String promptContextEn, Integer viewsCount, boolean active,
        LocalDateTime createdAt, LocalDateTime updatedAt,
        List<CasePublicDto.TagInfo> tags) {

    public static CaseAdminDto from(CaseEntity c, List<CasePublicDto.TagInfo> tags) {
        return new CaseAdminDto(c.getId(), c.getSlug(), c.getTitle(), c.getTitleEn(),
                c.getDescription(), c.getFullDescription(), c.getDifficulty(),
                c.getAverageSolveMin(), c.getPdfUrl(), c.getIconUrl(),
                c.getPromptContextEn(), c.getViewsCount(), c.getActive(),
                c.getCreatedAt(), c.getUpdatedAt(), tags);
    }
}