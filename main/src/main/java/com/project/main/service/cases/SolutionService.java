package com.project.main.service.cases;

import com.project.main.dto.event.SolutionSubmittedEvent;
import com.project.main.dto.integration.ChatMessageDto;
import com.project.main.dto.common.PageResponse;
import com.project.main.dto.integration.SubmitSolutionRequest;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.NotFoundException;
import com.project.main.model.common.Solution;
import com.project.main.repository.cases.CaseRepository;
import com.project.main.repository.user.LeaderboardRepository;
import com.project.main.repository.cases.SolutionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class SolutionService {

    private final SolutionRepository solutionRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CaseRepository caseRepository;

    public SolutionService(SolutionRepository solutionRepository,
                           LeaderboardRepository leaderboardRepository,
                           ApplicationEventPublisher eventPublisher,
                           CaseRepository caseRepository) {
        this.solutionRepository = solutionRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.eventPublisher = eventPublisher;
        this.caseRepository = caseRepository;
    }

    @Transactional
    public void submitSolution(Long userId, SubmitSolutionRequest request) {

        if (!caseRepository.existsById(request.getCaseId())) {
            throw new NotFoundException("Case not found");
        }

        Solution solution = new Solution();

        solution.setSolutionText(request.getSolutionText());
        solution.setSolutionResponse(request.getSolutionResponse());

        solution.setRating(request.getRating());

        solution.setCaseId(request.getCaseId());
        solution.setUserId(userId);
        solutionRepository.save(solution);

        leaderboardRepository.recalculateAndSetScore(userId);

        eventPublisher.publishEvent(new SolutionSubmittedEvent(
                userId,
                request.getCaseId(),
                request.getRating()
        ));

    }



    @Transactional(readOnly = true)
    public PageResponse<ChatMessageDto> getChatSequence(
            Long caseId,
            Long userId,
            int page,
            int size) {

        if (caseId == null || caseId <= 0) {
            throw new BadRequestException("Invalid case ID");
        }

        if (userId == null || userId <= 0) {
            throw new BadRequestException("Invalid user ID");
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

}
