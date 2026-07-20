package com.project.main.service;

import com.project.main.model.Solution;
import com.project.main.repository.LeaderboardRepository;
import com.project.main.repository.SolutionRepository;
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

    public List<Solution> getChatSequence(Long caseId, Long userId) {
        return solutionRepository.findByCaseIdAndUserIdOrderBySolutionIdAsc(caseId, userId);
    }
}
