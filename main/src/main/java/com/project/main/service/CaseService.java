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
    public CaseEntity createCase(CaseCreateRequest request,
                                 MultipartFile pdfFile,
                                 MultipartFile iconFile) {


        String pdfUrl = null;
        if (pdfFile != null && !pdfFile.isEmpty()) {
            pdfUrl = s3StorageService.uploadFile(pdfFile, "cases/pdfs");
        }

        String iconUrl = null;
        if (iconFile != null && !iconFile.isEmpty()) {
            iconUrl = s3StorageService.uploadFile(iconFile, "cases/icons");
        }

        CaseEntity newCase = new CaseEntity(
                request.getSlug(),
                request.getTitle(),
                request.getTitleEn(),
                request.getDescription(),
                request.getFullDescription(),
                request.getDifficulty(),
                request.getAverageSolveMin(),
                pdfUrl,
                iconUrl,
                request.getPromptContextEn(),
                true,
                request.getSortOrder(),
                0
        );


        return caseRepository.save(newCase);
    }
}