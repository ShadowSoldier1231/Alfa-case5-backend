package com.project.main.service;

import com.project.main.dto.ChatMessageDto;
import com.project.main.dto.PageResponse;
import com.project.main.exception.BadRequestException;
import com.project.main.model.Solution;
import com.project.main.repository.LeaderboardRepository;
import com.project.main.repository.SolutionRepository;
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

    public SolutionService(SolutionRepository solutionRepository,
                           LeaderboardRepository leaderboardRepository) {
        this.solutionRepository = solutionRepository;
        this.leaderboardRepository = leaderboardRepository;
    }

    @Transactional
    public void submitSolution(Long userId, Solution solution) {
        solution.setUserId(userId);
        solutionRepository.save(solution);

        Long score = solutionRepository.getSumOfMaxRatingsByUserId(userId);

        leaderboardRepository.updateScore(userId, score != null ? score : 0L);
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
