package com.project.main.service.user;


import com.project.main.dto.event.UserDeletedEvent;
import com.project.main.exception.NotFoundException;
import com.project.main.model.user.LeaderboardUser;
import com.project.main.model.user.UserSetup;
import com.project.main.repository.user.LeaderboardRepository;
import com.project.main.repository.user.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserModerationService {

    private final UserRepository userRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final ApplicationEventPublisher eventPublisher;


    public UserModerationService(UserRepository userRepository,
                                 LeaderboardRepository leaderboardRepository,
                                 ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.eventPublisher = eventPublisher;
    }



    @Transactional
    public String addWarning(Long userId) {
        LeaderboardUser user = leaderboardRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        user.setWarningsCount( (user.getWarningsCount() != null ? user.getWarningsCount() : 0L)
                + 1);

        if (user.getWarningsCount() > 2) {
            return this.banUser(user);
        }

        leaderboardRepository.save(user);
        return null;
    }

    @Transactional
    public String banUser(LeaderboardUser leaderboardUser) {
        if (leaderboardUser == null) {
            throw new NotFoundException("User does not exist");
        }

        if (leaderboardUser.getBanCount() != null && leaderboardUser.getBanCount() >= 3) {
            eventPublisher.publishEvent(new UserDeletedEvent(leaderboardUser.getUserId()));
            userRepository.deleteById(leaderboardUser.getUserId());
            return "User no longer exists";
        }

        UserSetup user = userRepository.findById(leaderboardUser.getUserId())
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        leaderboardUser.setBanCount( (leaderboardUser.getBanCount() != null ? leaderboardUser.getBanCount() : 0L)
                + 1L);
        leaderboardUser.setWarningsCount(0L);
        user.setBannedUntil(LocalDateTime.now().plusMonths(2));

        leaderboardRepository.save(leaderboardUser);
        userRepository.save(user);

        return "User is now banned";
    }



}
