package com.project.main.service;

import com.project.main.dto.CaseCreateRequest;
import com.project.main.model.CaseEntity;
import com.project.main.repository.CaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final S3StorageService s3StorageService;

    public CaseService(CaseRepository caseRepository, S3StorageService s3StorageService) {
        this.caseRepository = caseRepository;
        this.s3StorageService = s3StorageService;
    }

    @Transactional
    public void createCase(CaseCreateRequest request,
                           MultipartFile pdfFile,
                           MultipartFile iconFile) {

        String pdfKey = null;
        if (pdfFile != null && !pdfFile.isEmpty()) {
            pdfKey = s3StorageService.uploadFile(pdfFile, "cases/pdfs");
        }

        String iconKey = null;
        if (iconFile != null && !iconFile.isEmpty()) {
            iconKey = s3StorageService.uploadFile(iconFile, "cases/icons");
        }

        CaseEntity newCase = new CaseEntity(
                request.getSlug(),
                request.getTitle(),
                request.getTitleEn(),
                request.getDescription(),
                request.getFullDescription(),
                request.getDifficulty(),
                request.getAverageSolveMin(),
                pdfKey,
                iconKey,
                request.getPromptContextEn(),
                true,
                request.getSortOrder(),
                0
        );

        CaseEntity savedCase = caseRepository.save(newCase);
    }
}