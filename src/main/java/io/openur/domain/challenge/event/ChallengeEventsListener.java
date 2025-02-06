package io.openur.domain.challenge.event;

import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepositoryImpl;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeEventsListener {

    private final UserChallengeRepositoryImpl userChallengeRepository;

    private void airdropNFT(UserChallenge userChallenge) {
        // TODO: NFT 에어드랍
        userChallenge.setNftCompleted(true);
        userChallenge.setCompletedDate(LocalDateTime.now());
    }

    @Async
    @Transactional
    @EventListener
    public void handleOnDateChallengeEvent(OnDate event) {
        List<Long> completedUserChallengeIds = new ArrayList<>();
        
        for (var userChallenge : event.getUserChallenges()) {
            LocalDateTime dateCondition = userChallenge.getChallenge().getCompletedConditionDate();
            // TODO: Implement proper date condition check
            if (dateCondition != null && LocalDateTime.now().isAfter(dateCondition)) {
                airdropNFT(userChallenge);
                completedUserChallengeIds.add(userChallenge.getUserChallengeId());
            }
        }
        
        userChallengeRepository.bulkUpdateCompletedChallenges(completedUserChallengeIds);
    }

    @Async
    @Transactional
    @EventListener
    public void handleOnCountChallengeEvent(OnCount event) {
        List<Long> userChallengeIds = event.getUserChallenges().stream()
            .map(UserChallenge::getUserChallengeId)
            .toList();
            
        // First, increment all counts in one query
        userChallengeRepository.bulkIncrementCount(userChallengeIds);
        
        // Then process NFT airdrops for completed challenges
        List<Long> completedUserChallengeIds = new ArrayList<>();
        for (var userChallenge : event.getUserChallenges()) {
            Long countCondition = userChallenge.getChallenge().getCompletedConditionCount();
            Long currentCount = userChallenge.getCurrentCount() + 1; // Add 1 since we just incremented
            
            if (currentCount.equals(countCondition)) {
                airdropNFT(userChallenge);
                completedUserChallengeIds.add(userChallenge.getUserChallengeId());
            }
        }
        
        userChallengeRepository.bulkUpdateCompletedChallenges(completedUserChallengeIds);
    }

    @Async
    @Transactional
    @EventListener
    public void handleOnPlaceChallengeEvent(OnPlace event) {
        List<Long> completedUserChallengeIds = new ArrayList<>();
        
        for (var userChallenge : event.getUserChallenges()) {
            String placeCondition = userChallenge.getChallenge().getCompletedConditionPlace();
            // TODO: Implement proper place condition check
            if (placeCondition != null && placeCondition.equals(event.getLocation())) {
                airdropNFT(userChallenge);
                completedUserChallengeIds.add(userChallenge.getUserChallengeId());
            }
        }
        
        userChallengeRepository.bulkUpdateCompletedChallenges(completedUserChallengeIds);
    }

    @Async
    @Transactional
    @EventListener
    public void handleOnWearingChallengeEvent(OnWearing event) {
        List<Long> completedUserChallengeIds = new ArrayList<>();
        
        for (var userChallenge : event.getUserChallenges()) {
            Boolean wearingCondition = userChallenge.getChallenge().getCompletedConditionWearing();
            // TODO: Implement proper wearing condition check
            if (wearingCondition != null && wearingCondition) {
                airdropNFT(userChallenge);
                completedUserChallengeIds.add(userChallenge.getUserChallengeId());
            }
        }
        
        userChallengeRepository.bulkUpdateCompletedChallenges(completedUserChallengeIds);
    }
}

