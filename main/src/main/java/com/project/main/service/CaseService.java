package com.project.main.service;

import com.project.main.dto.CaseCreateRequest;
import com.project.main.dto.CasePromptResponse;
import com.project.main.dto.CaseUpdateRequest;
import com.project.main.dto.TagCreateRequest;
import com.project.main.exception.ApiException;
import com.project.main.model.CaseEntity;
import com.project.main.model.CaseTag;
import com.project.main.model.CaseTagId;
import com.project.main.model.Tag;
import com.project.main.repository.CaseRepository;
import com.project.main.repository.CaseTagRepository;
import com.project.main.repository.TagRepository;
import org.springframework.http.HttpStatus;
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
    private final TagRepository tagRepository;
    private final CaseTagRepository caseTagRepository;
    private final S3StorageService s3StorageService;

    public CaseService(CaseRepository caseRepository,
                       TagRepository tagRepository,
                       CaseTagRepository caseTagRepository,
                       S3StorageService s3StorageService) {
        this.caseRepository = caseRepository;
        this.tagRepository = tagRepository;
        this.caseTagRepository = caseTagRepository;
        this.s3StorageService = s3StorageService;
    }




    @Transactional
    public Long createTag(TagCreateRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }

        String tagName = request.getName().trim();

        if (tagRepository.existsByName(tagName)) {
            throw new IllegalArgumentException("Tag with this name already exists");
        }

        Tag newTag = new Tag();
        newTag.setName(tagName);
        newTag.setActive(true);

        Tag savedTag = tagRepository.save(newTag);
        return savedTag.getId();
    }

    @Transactional
    public void deactivateTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        tag.setActive(false);
        tagRepository.save(tag);
    }

    @Transactional
    public void attachTagToCase(Long caseId, Long tagId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        if (caseTagRepository.existsByCaseEntityIdAndTagId(caseId, tagId)) {
            throw new IllegalArgumentException("Tag is already attached to this case");
        }

        CaseTag caseTag = new CaseTag();
        caseTag.setId(new CaseTagId(caseId, tagId));
        caseTag.setCaseEntity(caseEntity);
        caseTag.setTag(tag);

        caseTagRepository.save(caseTag);
    }

    @Transactional
    public void detachTagFromCase(Long caseId, Long tagId) {
        if (!caseTagRepository.existsByCaseEntityIdAndTagId(caseId, tagId)) {
            throw new IllegalArgumentException("Tag is not attached to this case");
        }

        caseTagRepository.deleteByCaseEntityIdAndTagId(caseId, tagId);
    }

    @Transactional
    public Long createCase(CaseCreateRequest request,
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
        return  newCase.getId();
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
                    Long id = null;
                    String name = "Unknown";
                    long count = 0L;

                    if (row instanceof Object[] arr && arr.length >= 3) {
                        id = arr[0] != null ? ((Number) arr[0]).longValue() : null;
                        name = arr[1] != null ? arr[1].toString() : "Unknown";
                        count = arr[2] != null ? ((Number) arr[2]).longValue() : 0L;
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", id);
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


        if (pdfFile != null && !pdfFile.isEmpty()) {

            if (existingCase.getPdfUrl() != null) {
                s3StorageService.deleteFile(existingCase.getPdfUrl());
            }
            existingCase.setPdfUrl(s3StorageService.uploadFile(pdfFile, "cases/pdfs"));

        } else if (Boolean.TRUE.equals(req.getRemovePdf())) {

            if (existingCase.getPdfUrl() != null) {
                s3StorageService.deleteFile(existingCase.getPdfUrl());
            }
            existingCase.setPdfUrl(null);
        }


        if (iconFile != null && !iconFile.isEmpty()) {
            if (existingCase.getIconUrl() != null) {
                s3StorageService.deleteFile(existingCase.getIconUrl());
            }
            existingCase.setIconUrl(s3StorageService.uploadFile(iconFile, "cases/icons"));

        } else if (Boolean.TRUE.equals(req.getRemoveIcon())) {
            if (existingCase.getIconUrl() != null) {
                s3StorageService.deleteFile(existingCase.getIconUrl());
            }
            existingCase.setIconUrl(null);
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


    @Transactional(readOnly = true)
    public CasePromptResponse getCasePrompt(Long caseId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException("Case with ID: " + caseId + " is not found", HttpStatus.NOT_FOUND));

        if (!Boolean.TRUE.equals(caseEntity.getActive())) {
            throw new ApiException("Case is inactive", HttpStatus.NOT_FOUND);
        }

        String prompt = caseEntity.getPromptContextEn();

        return new CasePromptResponse(
                caseEntity.getTitle(),
                prompt != null ? prompt : "",
                caseEntity.getId()
        );
    }

}