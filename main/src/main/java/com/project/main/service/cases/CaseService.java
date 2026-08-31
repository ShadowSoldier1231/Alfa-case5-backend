package com.project.main.service.cases;

import com.project.main.dto.cases.*;
import com.project.main.dto.common.PageResponse;
import com.project.main.dto.learing.*;
import com.project.main.dto.tags.TagCreateRequest;
import com.project.main.dto.tags.TagListItem;
import com.project.main.dto.tags.TagUpdateRequest;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.ConflictException;
import com.project.main.exception.NotFoundException;
import com.project.main.model.cases.*;
import com.project.main.model.learning.StudyMaterial;
import com.project.main.repository.cases.*;
import com.project.main.repository.learning.StudyMaterialRepository;
import com.project.main.service.common.S3StorageService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    private final CaseCompletionRepository completionRepository;
    private final CaseRatingRepository caseRatingRepository;
    private final  StudyMaterialRepository materialRepository;

    public CaseService(CaseRepository caseRepository,
                       TagRepository tagRepository,
                       CaseTagRepository caseTagRepository,
                       S3StorageService s3StorageService,
                       CaseCompletionRepository completionRepository,
                       CaseRatingRepository caseRatingRepository,
                       StudyMaterialRepository materialRepository) {
        this.caseRepository = caseRepository;
        this.tagRepository = tagRepository;
        this.caseTagRepository = caseTagRepository;
        this.s3StorageService = s3StorageService;
        this.completionRepository = completionRepository;
        this.caseRatingRepository = caseRatingRepository;
        this.materialRepository = materialRepository;
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
    public void activateTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag not found"));

        tag.setActive(true);
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

        if (caseRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("Case with this slug already exists");
        }

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
                request.getActive() != null ? request.getActive() : true,
                0,
                request.getPerfectSolution()
        );

        caseRepository.save(newCase);
        return  newCase.getId();
    }

    @Transactional
    public CasePublicDto getCaseByIdAndIncrementViews(Long id) {
        CaseEntity c = caseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Case not found"));

        if (!Boolean.TRUE.equals(c.getActive())) {
            throw new NotFoundException("Case not found");
        }

        caseRepository.incrementViewsCount(id);

        Map<Long, List<CasePublicDto.TagInfo>> tags = loadTags(List.of(c));
        Double caseRating = loadRatings(List.of(c.getId()))
                .get(c.getId());

        return CasePublicDto.from(
                c,
                tags.getOrDefault(c.getId(), List.of()),
                caseRating
        );
    }





    @Transactional(readOnly = true)
    public PageResponse<CasePublicDto> getPublicCases(int page, int size, String search, String sort) {
        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        String searchTerm = null;

        if (search != null && !search.isBlank()) {
            searchTerm = escapeLikeWildcards(search.trim());
        }
        if (searchTerm != null && searchTerm.length() > 200) {
            throw new BadRequestException("Search query is too long");
        }

        Pageable pageable = PageRequest.of(page, size, buildPublicCaseSort(sort));

        Page<CaseEntity> casePage = caseRepository.findPublicCases(searchTerm, pageable);

        List<CaseEntity> cases = casePage.getContent();

        Map<Long, List<CasePublicDto.TagInfo>> tags = loadTags(cases);

        Map<Long, Double> ratings = loadRatings(
                cases.stream()
                        .map(CaseEntity::getId)
                        .toList()
        );

        List<CasePublicDto> items = cases.stream()
                .map(c -> CasePublicDto.from(
                        c,
                        tags.getOrDefault(c.getId(), List.of()),
                        ratings.get(c.getId())
                ))
                .toList();

        return new PageResponse<>(
                items,
                casePage.getNumber(),
                casePage.getSize(),
                casePage.getTotalElements(),
                casePage.getTotalPages()
        );
    }

    @Transactional
    public void updateCase(Long id, CaseUpdateRequest req, MultipartFile pdfFile, MultipartFile iconFile) {
        CaseEntity existingCase = caseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Case with ID: "+ id + " is not found"));
        if (req.getSlug() != null && !req.getSlug().equals(existingCase.getSlug())
                && caseRepository.existsBySlug(req.getSlug())) {
            throw new ConflictException("Case with this slug already exists");
        }

        if (pdfFile != null && !pdfFile.isEmpty()) {
            String newPdfKey = s3StorageService.uploadFile(pdfFile, "cases/pdfs");
            if (existingCase.getPdfUrl() != null) {
                s3StorageService.deleteFile(existingCase.getPdfUrl());
            }
            existingCase.setPdfUrl(newPdfKey);
        } else if (Boolean.TRUE.equals(req.getRemovePdf())) {

            if (existingCase.getPdfUrl() != null) {
                s3StorageService.deleteFile(existingCase.getPdfUrl());
            }
            existingCase.setPdfUrl(null);
        }


        if (iconFile != null && !iconFile.isEmpty()) {
            String newIconKey = s3StorageService.uploadFile(iconFile, "cases/icons");
            if (existingCase.getIconUrl() != null) {
                s3StorageService.deleteFile(existingCase.getIconUrl());
            }
            existingCase.setIconUrl(newIconKey);

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
        if (Boolean.TRUE.equals(req.getRemovePerfectSolution())) {
            existingCase.setPerfectSolution(null);
        } else if (req.getPerfectSolution() != null) {
            existingCase.setPerfectSolution(req.getPerfectSolution());
        }
        caseRepository.save(existingCase);
    }

    @Transactional(readOnly = true)
    public String getPerfectSolutionOrThrow(Long caseId, Long userId){

        CaseEntity c = caseRepository.findById(caseId)
                .orElseThrow(() -> new NotFoundException("Case not found"));

        if (!Boolean.TRUE.equals(c.getActive())) {
            throw new NotFoundException("Case not found");
        }
        if(!completionRepository.existsByUserIdAndCaseId(userId, caseId)){
            throw new BadRequestException("Case is not solved yet");
        }
        return c.getPerfectSolution();
    }

    public CasePromptResponse getCasePrompt(Long id) {
        CaseEntity c = caseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Case not found"));

        if (!Boolean.TRUE.equals(c.getActive())) {
            throw new NotFoundException("Case not found");
        }

        return new CasePromptResponse(c.getTitle(), c.getPromptContextEn(), c.getId());
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



    @Transactional
    public void rateCase(Long userId, Long caseId, Long rating) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException("Invalid user ID");
        }

        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new BadRequestException("Invalid rating value");
        }

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new NotFoundException("Case not found"));

        if (!Boolean.TRUE.equals(caseEntity.getActive())) {
            throw new NotFoundException("Case not found");
        }

        CaseRating caseRating = caseRatingRepository
                .findByUserIdAndCaseId(userId, caseId)
                .orElse(null);

        if (caseRating == null) {
            caseRating = new CaseRating();
            caseRating.setUserId(userId);
            caseRating.setCaseId(caseId);
        }

        caseRating.setRating(rating);

        caseRatingRepository.save(caseRating);
    }

    private Map<Long, Double> loadRatings(List<Long> caseIds) {
        if (caseIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> result = new HashMap<>();

        for (Object[] row : caseRatingRepository.findAverageRatingsByCaseIds(caseIds)) {
            Long caseId = ((Number) row[0]).longValue();
            Double avg = row[1] == null ? null : ((Number) row[1]).doubleValue();
            result.put(caseId, avg);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public PageResponse<TagListItem> getAdminTags(int page, int size, String search, String sort) {
        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        Sort sortBy = Sort.by(Sort.Direction.DESC, "created_at");

        if (sort != null && !sort.isBlank()) {
            String[] sortParts = sort.split(",");
            String property = sortParts[0].trim();

            Sort.Direction direction = Sort.Direction.ASC;
            if (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1].trim())) {
                direction = Sort.Direction.DESC;
            }

            String sortColumn = switch (property.toLowerCase()) {
                case "id" -> "id";
                case "name" -> "name";
                case "active" -> "is_active";
                case "casecount", "case_count", "casescount", "cases_count" -> "case_count";
                case "createdat", "created_at" -> "created_at";
                default -> "created_at";
            };

            sortBy = Sort.by(direction, sortColumn);
        }

        Pageable pageable = PageRequest.of(page, size, sortBy);

        String searchTerm = null;

        if (search != null && !search.isBlank()) {
            searchTerm = escapeLikeWildcards(search.trim());
        }
        if (searchTerm != null && searchTerm.length() > 200) {
            throw new BadRequestException("Search query is too long");
        }

        Page<Object[]> tagPage = tagRepository.findAdminTagsWithCaseCount(searchTerm, pageable);

        List<TagListItem> items = tagPage.getContent().stream()
                .map(row -> {
                    Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
                    String name = row[1] != null ? row[1].toString() : null;
                    Boolean active = toBoolean(row[2]);
                    Long caseCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;

                    return new TagListItem(id, name, active, caseCount);
                })
                .toList();

        return new PageResponse<>(
                items,
                tagPage.getNumber(),
                tagPage.getSize(),
                tagPage.getTotalElements(),
                tagPage.getTotalPages()
        );
    }
    @Transactional
    public void updateTag(Long tagId, TagUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Request cannot be empty");
        }

        boolean hasName = request.getName() != null;
        boolean hasActive = request.getActive() != null;

        if (!hasName && !hasActive) {
            throw new BadRequestException("No fields to update");
        }

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new NotFoundException("Tag not found"));

        if (hasName) {
            String newName = request.getName().trim();

            if (newName.isBlank()) {
                throw new BadRequestException("Tag name cannot be empty");
            }

            if (newName.length() > 100) {
                throw new BadRequestException("Tag name must be between 1 and 100 characters");
            }

            if (!newName.equals(tag.getName()) && tagRepository.existsByName(newName)) {
                throw new ConflictException("Tag with this name already exists");
            }

            tag.setName(newName);
        }

        if (hasActive) {
            tag.setActive(request.getActive());
        }

        tagRepository.save(tag);
    }


    @Transactional(readOnly = true)
    public PageResponse<CasePublicDto.TagInfo> getPublicTags(int page, int size, String search, String sort) {
        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        String searchTerm = null;

        if (search != null && !search.isBlank()) {
            searchTerm = escapeLikeWildcards(search.trim());
        }
        if (searchTerm != null && searchTerm.length() > 200) {
            throw new BadRequestException("Search query is too long");
        }

        Pageable pageable = PageRequest.of(page, size, buildPublicTagSort(sort));

        Page<Object[]> tagPage = tagRepository.findPublicTagsWithCaseCount(searchTerm, pageable);

        List<CasePublicDto.TagInfo> items = tagPage.getContent().stream()
                .map(row -> new CasePublicDto.TagInfo(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();

        return new PageResponse<>(
                items,
                tagPage.getNumber(),
                tagPage.getSize(),
                tagPage.getTotalElements(),
                tagPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public MaterialDto getAllMaterialByCaseId(Long caseId) {
        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        if (!caseRepository.existsActiveCaseById(caseId)) {
            throw new NotFoundException("Case not found");
        }

        List<Object[]> materials =
                materialRepository.findActiveByCaseIdSorted(caseId);

        return new MaterialDto(
                caseId,
                materials.stream()
                        .map(
                                row -> {
                                    Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
                                    String title = row[1] != null ? (String) row[1] : null;
                                    Integer position = row[2] != null ? ((Number) row[2]).intValue() : null;
                                    return new MaterialDto.MaterialPart(id, title, position);
                                }


                        )
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public PartialMaterialDto getMaterialById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid material ID");
        }

        StudyMaterial s = materialRepository.findActiveByIdAndActiveCase(id)
                .orElseThrow(() -> new NotFoundException("Material not found"));

        return new PartialMaterialDto(
                s.getId(),
                s.getCaseId(),
                s.getTitle(),
                s.getText(),
                s.getPosition()
        );
    }

    @Transactional(readOnly = true)
    public AdminMaterialDto getAdminMaterials(Long caseId) {
        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        if (!caseRepository.existsCaseById(caseId)) {
            throw new NotFoundException("Case not found");
        }

        List<Object[]> materials =
                materialRepository.findAllByCaseIdOrdered(caseId);

        return new AdminMaterialDto(
                caseId,
                materials.stream()
                        .map(
                                row -> {
                                    Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
                                    String title = row[1] != null ? (String) row[1] : null;
                                    Integer position = row[2] != null ? ((Number) row[2]).intValue() : null;
                                    Boolean active = toBoolean(row[3]);
                                    return new AdminMaterialDto.AdminMaterialPart(
                                            id, title, position, active
                                    );
                                }
                        )
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public AdminPartialMaterialDto getAdminMaterialById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid material ID");
        }

        List<Object[]> result = materialRepository.findAdminMaterialById(id);
        if (result.isEmpty()) {
            throw new NotFoundException("Material not found");
        }

        Object[] row = result.get(0);

        Long materialId = row[0] != null ? ((Number) row[0]).longValue() : null;
        Long caseId = row[1] != null ? ((Number) row[1]).longValue() : null;
        String title = row[2] != null ? row[2].toString() : null;
        Integer position = row[3] != null ? ((Number) row[3]).intValue() : null;
        String text = row[4] != null ? row[4].toString() : null;
        Boolean active = toBoolean(row[5]);

        return new AdminPartialMaterialDto(materialId, caseId, title, position, text, active);
    }

    @Transactional
    public Long createTheoryMaterial(Long caseId, TheoryCreateRequest request) {
        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        if (request == null) {
            throw new BadRequestException("Invalid request");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Title cannot be empty");
        }

        if (request.getTitle().length() > 255) {
            throw new BadRequestException("Title too long (max 255)");
        }

        if (request.getPosition() == null) {
            throw new BadRequestException("Position is required");
        }

        if (request.getPosition() < 1) {
            throw new BadRequestException("Position must be at least 1");
        }

        if (request.getText() == null || request.getText().isBlank()) {
            throw new BadRequestException("Text cannot be empty");
        }

        if (!caseRepository.existsCaseById(caseId)) {
            throw new NotFoundException("Case not found");
        }

        if (materialRepository.existsByCaseIdAndPosition(caseId, request.getPosition())) {
            throw new ConflictException("Material with this position already exists");
        }

        StudyMaterial material = new StudyMaterial(
                caseId,
                request.getText(),
                request.getTitle(),
                request.getPosition(),
                request.getActive()
        );

        try {
            return materialRepository.saveAndFlush(material).getId();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Material with this position already exists");
        }
    }

    @Transactional
    public void updateTheoryMaterial(Long id, TheoryUpdateRequest request) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid material ID");
        }

        if (request == null) {
            throw new BadRequestException("Request cannot be empty");
        }

        boolean hasTitle = request.getTitle() != null;
        boolean hasPosition = request.getPosition() != null;
        boolean hasText = request.getText() != null;
        boolean hasActive = request.getActive() != null;

        if (!hasTitle && !hasPosition && !hasText && !hasActive) {
            throw new BadRequestException("No fields to update");
        }

        if (hasTitle) {
            String title = request.getTitle().trim();

            if (title.isBlank()) {
                throw new BadRequestException("Title cannot be empty");
            }

            if (title.length() > 255) {
                throw new BadRequestException("Title too long (max 255)");
            }
        }

        if (hasPosition && request.getPosition() < 1) {
            throw new BadRequestException("Position must be at least 1");
        }

        if (hasText && request.getText().isBlank()) {
            throw new BadRequestException("Text cannot be empty");
        }

        StudyMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Material not found"));

        if (hasPosition && !request.getPosition().equals(material.getPosition())) {
            if (materialRepository.existsByCaseIdAndPositionAndIdNot(
                    material.getCaseId(),
                    request.getPosition(),
                    id
            )) {
                throw new ConflictException("Material with this position already exists");
            }

            material.setPosition(request.getPosition());
        }

        if (hasTitle) {
            material.setTitle(request.getTitle().trim());
        }

        if (hasText) {
            material.setText(request.getText());
        }

        if (hasActive) {
            material.setActive(request.getActive());
        }

        try {
            materialRepository.saveAndFlush(material);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Material with this position already exists");
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<CaseAdminDto> getAdminCases(int page, int size, String search, String sort) {
        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        String searchTerm = null;

        if (search != null && !search.isBlank()) {
            searchTerm = escapeLikeWildcards(search.trim());
        }
        if (searchTerm != null && searchTerm.length() > 200) {
            throw new BadRequestException("Search query is too long");
        }

        Pageable pageable = PageRequest.of(page, size, buildCaseSort(sort));

        Page<CaseEntity> casePage = caseRepository.findAdminCases(searchTerm, pageable);

        List<CaseEntity> cases = casePage.getContent();

        Map<Long, List<CasePublicDto.TagInfo>> tags = loadTags(cases);

        Map<Long, Double> ratings = loadRatings(
                cases.stream()
                        .map(CaseEntity::getId)
                        .toList()
        );

        List<CaseAdminDto> items = cases.stream()
                .map(c -> CaseAdminDto.from(
                        c,
                        tags.getOrDefault(c.getId(), List.of()),
                        ratings.get(c.getId())
                ))
                .toList();

        return new PageResponse<>(
                items,
                casePage.getNumber(),
                casePage.getSize(),
                casePage.getTotalElements(),
                casePage.getTotalPages()
        );
    }







    private Sort buildPublicTagSort(String sort) {
        Sort sortBy = Sort.by(
                Sort.Order.desc("case_count"),
                Sort.Order.asc("name")
        );

        if (sort == null || sort.isBlank()) {
            return sortBy;
        }

        String[] sortParts = sort.split(",");
        String property = sortParts[0].trim();

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        String sortColumn = switch (property.toLowerCase()) {
            case "id" -> "id";
            case "name" -> "name";
            case "count", "casecount", "case_count", "casescount", "cases_count" -> "case_count";
            default -> "case_count";
        };

        Sort result = Sort.by(direction, sortColumn);

        if (!sortColumn.equals("name")) {
            result = result.and(Sort.by(Sort.Direction.ASC, "name"));
        }

        return result;
    }

    private Sort buildPublicCaseSort(String sort) {
        Sort sortBy = Sort.by(Sort.Direction.DESC, "created_at");

        if (sort == null || sort.isBlank()) {
            return sortBy;
        }

        String[] sortParts = sort.split(",");
        String property = sortParts[0].trim();

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        String sortColumn = switch (property.toLowerCase()) {
            case "id" -> "id";
            case "slug" -> "slug";
            case "title" -> "title";
            case "titleen", "title_en" -> "title_en";
            case "difficulty" -> "difficulty";
            case "averagesolvemin", "average_solve_min", "averagesolve", "average" -> "average_solve_min";
            case "views", "viewscount", "views_count" -> "views_count";
            case "createdat", "created_at" -> "created_at";
            case "updatedat", "updated_at" -> "updated_at";
            default -> "created_at";
        };

        return Sort.by(direction, sortColumn);
    }

    private Sort buildCaseSort(String sort) {
        Sort sortBy = Sort.by(Sort.Direction.DESC, "created_at");

        if (sort == null || sort.isBlank()) {
            return sortBy;
        }

        String[] sortParts = sort.split(",");
        String property = sortParts[0].trim();

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        String sortColumn = switch (property.toLowerCase()) {
            case "id" -> "id";
            case "slug" -> "slug";
            case "title" -> "title";
            case "titleen", "title_en" -> "title_en";
            case "difficulty" -> "difficulty";
            case "averagesolvemin", "average_solve_min", "averagesolve", "average" -> "average_solve_min";
            case "views", "viewscount", "views_count" -> "views_count";
            case "active", "isactive", "is_active" -> "is_active";
            case "createdat", "created_at" -> "created_at";
            case "updatedat", "updated_at" -> "updated_at";
            default -> "created_at";
        };

        return Sort.by(direction, sortColumn);
    }



    private String escapeLikeWildcards(String value) {
        if (value == null) {
            return null;
        }

        return value
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean b) {
            return b;
        }

        if (value instanceof Number n) {
            return n.intValue() != 0;
        }

        String s = value.toString().trim().toLowerCase();

        return switch (s) {
            case "true", "t", "1", "yes", "y" -> true;
            case "false", "f", "0", "no", "n" -> false;
            default -> null;
        };
    }
}