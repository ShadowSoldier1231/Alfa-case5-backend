package com.project.main.service;

import com.project.main.dto.CaseCreateRequest;
import com.project.main.dto.CaseUpdateRequest;
import com.project.main.model.CaseEntity;
import com.project.main.repository.CaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                0
        );

        caseRepository.save(newCase);
    }
    @Transactional
    public CaseEntity getCaseByIdAndIncrementViews(Long id) {
        CaseEntity caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Кейс не найден"));

        caseRepository.incrementViewsCount(id);
        return caseEntity;
    }

    public List<Map<String, Object>> getActiveTagsWithCount() {
        return caseRepository.findActiveTagsWithCaseCount().stream()
                .map(row -> {
                    String name = "Unknown";
                    long count = 0L;

                    if (row instanceof Object[] arr && arr.length >= 2) {
                        name = arr[0] != null ? arr[0].toString() : "Unknown";
                        count = arr[1] != null ? ((Number) arr[1]).longValue() : 0L;
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("count", count);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<CaseEntity> getAllPublicCases() {
        return caseRepository.findAllByIsActiveTrueOrderByCreatedAtDesc();
    }

    public List<CaseEntity> getAllAdminCases() {
        return caseRepository.findAllByOrderByCreatedAtDesc();
    }
    @Transactional
    public void updateCase(Long id, CaseUpdateRequest req, MultipartFile pdfFile, MultipartFile iconFile) {
        CaseEntity existingCase = caseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Кейс не найден"));


        if (req.getRemovePdf()) {
            s3StorageService.deleteFile(existingCase.getPdfUrl());
            existingCase.setPdfUrl(null);
        } else if (pdfFile != null && !pdfFile.isEmpty()) {
            s3StorageService.deleteFile(existingCase.getPdfUrl());
            existingCase.setPdfUrl(s3StorageService.uploadFile(pdfFile, "cases/pdfs"));
        }


        if (req.getRemoveIcon()) {
            s3StorageService.deleteFile(existingCase.getIconUrl());
            existingCase.setIconUrl(null);
        } else if (iconFile != null && !iconFile.isEmpty()) {
            s3StorageService.deleteFile(existingCase.getIconUrl());
            existingCase.setIconUrl(s3StorageService.uploadFile(iconFile, "cases/icons"));
        }


        if (req.getSlug() != null) existingCase.setSlug(req.getSlug());
        if (req.getTitle() != null) existingCase.setTitle(req.getTitle());
        if (req.getTitleEn() != null) existingCase.setTitleEn(req.getTitleEn());
        if (req.getDescription() != null) existingCase.setDescription(req.getDescription());
        if (req.getFullDescription() != null) existingCase.setFullDescription(req.getFullDescription());
        if (req.getDifficulty() != null) existingCase.setDifficulty(req.getDifficulty());
        if (req.getAverageSolveMin() != null) existingCase.setAverageSolveMin(req.getAverageSolveMin());
        if (req.getPromptContextEn() != null) existingCase.setPromptContextEn(req.getPromptContextEn());
        if (req.getActive() != null) existingCase.setActive(req.getActive());

        caseRepository.save(existingCase);
    }


}