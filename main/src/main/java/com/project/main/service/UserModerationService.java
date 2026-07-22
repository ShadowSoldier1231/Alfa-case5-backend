package com.project.main.service;


import com.project.main.dto.UserDeletedEvent;
import com.project.main.model.LeaderboardUser;
import com.project.main.model.UserSetup;
import com.project.main.repository.LeaderboardRepository;
import com.project.main.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
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
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));

        user.setWarningsCount(user.getWarningsCount() + 1);

        if (user.getWarningsCount() > 2) {

            return this.banUser(user);
        }

        leaderboardRepository.save(user);
        return null;
    }


    @Transactional
    public String banUser(LeaderboardUser leaderboardUser){


        if(leaderboardUser == null) return  "User does not exist";

        if (leaderboardUser.getBanCount() >= 3){
            eventPublisher.publishEvent(new UserDeletedEvent(leaderboardUser.getUserId()));
            return  "User no longer exists";
        }
        UserSetup user = userRepository.findById(leaderboardUser.getUserId()).orElse(null);
        if(user == null){
            return  "User does not exist";
        }
        leaderboardUser.setBanCount(leaderboardUser.getBanCount() + 1L);
        leaderboardUser.setWarningsCount(0L);
        user.setBannedUntil(LocalDateTime.now().plusMonths(2));
        leaderboardRepository.save(leaderboardUser);
        userRepository.save(user);

        return "User is now banned";
    }



}
