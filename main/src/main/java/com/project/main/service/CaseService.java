package com.project.main.service;

import com.project.main.dto.*;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.ConflictException;
import com.project.main.exception.NotFoundException;
import com.project.main.model.*;
import com.project.main.repository.CaseRepository;
import com.project.main.repository.CaseTagRepository;
import com.project.main.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
                0
        );

        caseRepository.save(newCase);
        return  newCase.getId();
    }

    @Transactional
    public CasePublicDto getCaseByIdAndIncrementViews(Long id) {
        caseRepository.incrementViewsCount(id);
        CaseEntity c = caseRepository.findById(id).orElseThrow(() -> new NotFoundException("Case not found"));
        return CasePublicDto.from(c,
                loadTags(List.of(c)).getOrDefault(id, List.of())
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

        Map<Long, List<CasePublicDto.TagInfo>> tags = loadTags(casePage.getContent());

        List<CasePublicDto> items = casePage.getContent().stream()
                .map(c -> CasePublicDto.from(
                        c,
                        tags.getOrDefault(c.getId(), List.of())
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
                    Boolean active = row[2] instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(row[2]));
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

        Map<Long, List<CasePublicDto.TagInfo>> tags = loadTags(casePage.getContent());

        List<CaseAdminDto> items = casePage.getContent().stream()
                .map(c -> CaseAdminDto.from(
                        c,
                        tags.getOrDefault(c.getId(), List.of())
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

}