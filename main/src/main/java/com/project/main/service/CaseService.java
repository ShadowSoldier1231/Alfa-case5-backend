package com.project.main.service;

import com.project.main.dto.*;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.ConflictException;
import com.project.main.exception.NotFoundException;
import com.project.main.model.*;
import com.project.main.repository.CaseRepository;
import com.project.main.repository.CaseTagRepository;
import com.project.main.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
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
            throw new BadRequestException("Tag name cannot be empty");
        }

        String tagName = request.getName().trim();

        if (tagRepository.existsByName(tagName)) {
            throw new ConflictException("Tag with this name already exists");
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
                .orElseThrow(() -> new NotFoundException("Tag not found"));

        tag.setActive(false);
        tagRepository.save(tag);
    }

    @Transactional
    public void attachTagToCase(Long caseId, Long tagId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new NotFoundException("Case not found"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag not found"));

        if (caseTagRepository.existsByCaseEntityIdAndTagId(caseId, tagId)) {
            throw new ConflictException("Tag is already attached to this case");
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
            throw new BadRequestException("Tag is not attached to this case");
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
    public CasePublicDto getCaseByIdAndIncrementViews(Long id) {
        caseRepository.incrementViewsCount(id);
        CaseEntity c = caseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Кейс не найден"));
        return CasePublicDto.from(c, loadTags(List.of(c)).getOrDefault(id, List.of()));
    }

    public List<Map<String, Object>> getActiveTagsWithCount() {
        return caseRepository.getActiveTagsWithCaseCount().stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", ((Number) row[0]).longValue());
                    m.put("name", row[1]);
                    m.put("count", ((Number) row[2]).longValue());
                    return m;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CasePublicDto> getAllPublicCases() {
        List<CaseEntity> cases = caseRepository.findAllActive();
        Map<Long, List<CasePublicDto.TagInfo>> tags = loadTags(cases);
        return cases.stream()
                .map(c -> CasePublicDto.from(c, tags.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CaseAdminDto> getAllAdminCases() {
        List<CaseEntity> cases = caseRepository.findAllForAdmin();
        Map<Long, List<CasePublicDto.TagInfo>> tags = loadTags(cases);
        return cases.stream()
                .map(c -> CaseAdminDto.from(c, tags.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    @Transactional
    public void updateCase(Long id, CaseUpdateRequest req, MultipartFile pdfFile, MultipartFile iconFile) {
        CaseEntity existingCase = caseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Case with ID: "+ id + " is not found"));


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


    public CasePromptResponse getCasePrompt(Long id) {
        CaseEntity c = caseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Case is not found"));
        return new CasePromptResponse(c.getTitle(), c.getPromptContextEn(),c.getId());
    }


    private Map<Long, List<CasePublicDto.TagInfo>> loadTags(List<CaseEntity> cases) {
        if (cases.isEmpty()) return Map.of();
        List<Long> ids = cases.stream().map(CaseEntity::getId).toList();
        return caseRepository.findTagsByCaseIds(ids).stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row[0]).longValue(),
                        Collectors.mapping(
                                row -> new CasePublicDto.TagInfo(
                                        ((Number) row[1]).longValue(),
                                        (String) row[2],
                                        ((Number) row[3]).longValue()),
                                Collectors.toList())));
    }

}