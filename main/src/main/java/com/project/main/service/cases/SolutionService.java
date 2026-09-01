package com.project.main.service.cases;

import com.project.main.dto.event.SolutionSubmittedEvent;
import com.project.main.dto.integration.ChatMessageDto;
import com.project.main.dto.common.PageResponse;
import com.project.main.dto.integration.SolvingStatusResponse;
import com.project.main.dto.integration.SubmitSolutionRequest;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.NotFoundException;
import com.project.main.model.cases.CaseCompletion;
import com.project.main.model.cases.CaseEntity;
import com.project.main.model.cases.Solution;
import com.project.main.repository.cases.CaseCompletionRepository;
import com.project.main.repository.cases.CaseRepository;
import com.project.main.repository.user.LeaderboardRepository;
import com.project.main.repository.cases.SolutionRepository;
import com.project.main.service.component.TypeMapperComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;


@Service
public class SolutionService {

    private final SolutionRepository solutionRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CaseRepository caseRepository;
    private final StringRedisTemplate redisTemplate;
    private final CaseCompletionRepository completionRepository;
    private final TypeMapperComponent typeMapper;

    private static final Logger logger = LoggerFactory.getLogger(SolutionService.class);

    private static final String REDIS_START_TIME_PREFIX = "solve:start:";

    public SolutionService(SolutionRepository solutionRepository,
                           LeaderboardRepository leaderboardRepository,
                           ApplicationEventPublisher eventPublisher,
                           CaseRepository caseRepository,
                           StringRedisTemplate redisTemplate,
                            CaseCompletionRepository caseCompletionRepository,
                           TypeMapperComponent typeMapper) {
        this.solutionRepository = solutionRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.eventPublisher = eventPublisher;
        this.caseRepository = caseRepository;
        this.redisTemplate = redisTemplate;
        this.completionRepository = caseCompletionRepository;
        this.typeMapper = typeMapper;
    }

    @Transactional
    public void submitSolution(Long userId, SubmitSolutionRequest request) {

        if (!caseRepository.existsActiveCaseById(request.getCaseId())) {
            throw new NotFoundException("Case not found");
        }

        Integer timeSpentMinutes = null;
        String redisKey = REDIS_START_TIME_PREFIX + userId + ":" + request.getCaseId();
        String startTimeStr = redisTemplate.opsForValue().get(redisKey);
        if (startTimeStr != null) {
            try {
                Instant startTime = typeMapper.parseTimeToInstant(startTimeStr);

                if (startTime != null) {
                    long minutes = Duration.between(startTime, Instant.now()).toMinutes();
                    timeSpentMinutes = (int) Math.max(0, minutes);
                } else {
                    logger.warn("Start time is null for key: {}", redisKey);
                }
            } catch (DateTimeParseException e) {
                logger.error("Failed to parse start time from Redis for key: {}", redisKey);
            } catch (Exception e) {
                logger.error("Unexpected error when parsing date for key: {}, value: {}, error: {}", redisKey, startTimeStr, e.getMessage());
            }
        }



        Solution solution = new Solution();
        solution.setSolutionText(request.getSolutionText());
        solution.setSolutionResponse(request.getSolutionResponse());
        solution.setRating(request.getRating());
        solution.setCaseId(request.getCaseId());
        solution.setUserId(userId);
        solution.setTimeSpentMinutes(timeSpentMinutes);

        solutionRepository.save(solution);

        leaderboardRepository.recalculateAndSetScore(userId);

        eventPublisher.publishEvent(new SolutionSubmittedEvent(
                userId,
                request.getCaseId(),
                request.getRating(),
                timeSpentMinutes
        ));

    }

    public SolvingStatusResponse startSolving(Long userId, Long caseId) {

        if (userId == null || userId <= 0) {
            throw new BadRequestException("Invalid user ID");
        }
        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        CaseEntity c = caseRepository.findById(caseId).orElseThrow(() -> new NotFoundException("Case not found"));
        if (!Boolean.TRUE.equals(c.getActive())) {
            throw new NotFoundException("Case not found");
        }

        if (completionRepository.existsByUserIdAndCaseId(userId, caseId)) {
            throw new BadRequestException("Case is already solved");
        }

        String key = REDIS_START_TIME_PREFIX + userId + ":" + caseId;
        String startTime = Instant.now().toString();

        Boolean isSet = redisTemplate.opsForValue()
                .setIfAbsent(key, startTime, Duration.ofHours(24));

        if (Boolean.FALSE.equals(isSet)) {
            Long bestRating = solutionRepository.getMaxRatingByCaseIdAndUserId(caseId, userId);
            String existingTimeStr = redisTemplate.opsForValue().get(key);
            Instant existingTime = typeMapper.parseTimeToInstant(existingTimeStr);

            if (existingTime == null) {
                redisTemplate.delete(key);
                return new SolvingStatusResponse(false, null, false, bestRating);
            }

            return new SolvingStatusResponse(true, existingTime, false, bestRating);
        }

        return new SolvingStatusResponse(true, Instant.now(), false, 0L);
    }

    public SolvingStatusResponse getSolvingStatus(Long userId, Long caseId) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException("Invalid user ID");
        }

        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        if (!caseRepository.existsActiveCaseById(caseId)) {
            throw new NotFoundException("Case not found");
        }

        boolean isCompleted = completionRepository.existsByUserIdAndCaseId(userId, caseId);
        Long bestRating = solutionRepository.getMaxRatingByCaseIdAndUserId(caseId, userId);

        String key = REDIS_START_TIME_PREFIX + userId + ":" + caseId;
        String timeStr = redisTemplate.opsForValue().get(key);

        if (timeStr != null) {
            return new SolvingStatusResponse(true, typeMapper.parseTimeToInstant(timeStr), isCompleted, bestRating);
        }

        return new SolvingStatusResponse(false, null, isCompleted, bestRating);
    }

    @Transactional
    public SolvingStatusResponse finishSolving(Long userId, Long caseId) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException("Invalid user ID");
        }
        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        CaseEntity c = caseRepository.findById(caseId).orElseThrow(() -> new NotFoundException("Case not found"));
        if (!Boolean.TRUE.equals(c.getActive())) {
            throw new NotFoundException("Case not found");
        }

        boolean alreadyCompleted = completionRepository.existsByUserIdAndCaseId(userId, caseId);

        if (!alreadyCompleted) {
            CaseCompletion completion = new CaseCompletion(userId, caseId);
            completionRepository.save(completion);
        }

        String key = REDIS_START_TIME_PREFIX + userId + ":" + caseId;
        redisTemplate.delete(key);

        Long bestRating = solutionRepository.getMaxRatingByCaseIdAndUserId(caseId, userId);

        return new SolvingStatusResponse(false, null, true, bestRating);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatMessageDto> getChatSequence(
            Long caseId,
            Long userId,
            int page,
            int size) {


        if (userId == null || userId <= 0) {
            throw new BadRequestException("Invalid user ID");
        }

        if (!leaderboardRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "solution_id")
        );

        Page<Solution> solutionPage = solutionRepository.findChatSequence(
                caseId,
                userId,
                pageable
        );

        List<ChatMessageDto> items = solutionPage.getContent().stream()
                .map(ChatMessageDto::from)
                .toList();

        return new PageResponse<>(
                items,
                solutionPage.getNumber(),
                solutionPage.getSize(),
                solutionPage.getTotalElements(),
                solutionPage.getTotalPages()
        );
    }



    @Transactional(readOnly = true)
    public PageResponse<ChatMessageDto> getAllSolutionsForUser(Long userId, int page, int size) {

        if (userId == null || userId <= 0) {
            throw new BadRequestException("Invalid user ID");
        }

        if (!leaderboardRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "solution_id")
        );

        Page<Solution> solutionPage = solutionRepository.getAllSolutionsForUser(
                userId,
                pageable
        );

        List<ChatMessageDto> items = solutionPage.getContent().stream()
                .map(ChatMessageDto::from)
                .toList();

        return new PageResponse<>(
                items,
                solutionPage.getNumber(),
                solutionPage.getSize(),
                solutionPage.getTotalElements(),
                solutionPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatMessageDto> getAllSolutionsForCase(Long caseId, int page, int size) {
        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        if (!caseRepository.existsById(caseId)) {
            throw new NotFoundException("Case not found");
        }

        if (page < 0) {
            throw new BadRequestException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "solution_id")
        );

        Page<Solution> solutionPage = solutionRepository.getAllSolutionsForCase(
                caseId,
                pageable
        );

        List<ChatMessageDto> items = solutionPage.getContent().stream()
                .map(ChatMessageDto::from)
                .toList();

        return new PageResponse<>(
                items,
                solutionPage.getNumber(),
                solutionPage.getSize(),
                solutionPage.getTotalElements(),
                solutionPage.getTotalPages()
        );

    }
}
