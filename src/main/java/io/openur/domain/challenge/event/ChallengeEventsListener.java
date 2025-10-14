package io.openur.domain.challenge.event;

import io.openur.domain.challenge.dto.GeneralChallengeDto;
import io.openur.domain.challenge.model.Challenge;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
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

    private final UserChallengeRepository userChallengeRepository;

    private void airdropNFT(UserChallenge userChallenge) {
        // TODO: NFT 에어드랍
        userChallenge.setNftCompleted(true);
        userChallenge.setCompletedDate(LocalDateTime.now());
    }

    @Async
    @Transactional
    @EventListener
    public void handleOnDateChallengeEvent(GeneralChallengeDto.OnDate event) {
        List<Long> completedUserChallengeIds = new ArrayList<>();
        
        for (UserChallenge userChallenge : event.getUserChallenges()) {
            Challenge challenge = userChallenge.getChallengeStage();
            LocalDateTime dateCondition = challenge.getCompletedConditionDate();
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
    public void handleOnCountChallengeEvent(GeneralChallengeDto.OnCount event) {
        List<Long> userChallengeIds = event.getUserChallenges().stream()
            .map(UserChallenge::getUserChallengeId)
            .toList();
            
        // First, increment all counts in one query
        userChallengeRepository.bulkIncrementCount(userChallengeIds);
        
        // Then process NFT airdrops for completed challenges
        List<Long> completedUserChallengeIds = new ArrayList<>();
        for (UserChallenge userChallenge : event.getUserChallenges()) {
            Challenge challenge = userChallenge.getChallengeStage();
            Integer countCondition = challenge.getCompletedConditionCount();
            Integer currentCount = userChallenge.getCurrentCount() + 1; // Add 1 since we just incremented
            
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
    public void handleOnPlaceChallengeEvent(GeneralChallengeDto.OnPlace event) {
        List<Long> completedUserChallengeIds = new ArrayList<>();
        
        for (UserChallenge userChallenge : event.getUserChallenges()) {
            Challenge challenge = userChallenge.getChallengeStage();
            String placeCondition = challenge.getCompletedConditionText();
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
    public void handleOnWearingChallengeEvent(GeneralChallengeDto.OnWearing event) {
        List<Long> completedUserChallengeIds = new ArrayList<>();
        
        for (UserChallenge userChallenge : event.getUserChallenges()) {
            Challenge challenge = userChallenge.getChallengeStage();
//            Boolean wearingCondition = challenge.getCompletedConditionText();
//            // TODO: 옷 정보 불/합치 정보는 NFT 완성 이후 논의 필요
//            if (wearingCondition != null && wearingCondition) {
//                airdropNFT(userChallenge);
//                completedUserChallengeIds.add(userChallenge.getUserChallengeId());
//            }
        }
        
        userChallengeRepository.bulkUpdateCompletedChallenges(completedUserChallengeIds);
    }
}

