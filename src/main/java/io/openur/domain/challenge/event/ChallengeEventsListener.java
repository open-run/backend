package io.openur.domain.challenge.event;

import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.challenge.repository.ChallengeStageRepository;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeEventsListener {
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeStageRepository stageRepository;

    @EventListener
    public void handleChallengeRaise(OnRaise event) {
        userChallengeRepository.bulkIncrementCount(
            event.getUserChallenges().stream()
                .map(UserChallenge::getUserChallengeId)
                .toList()
        );
    }

    @EventListener
    public void handleChallengeEvolution(OnEvolution event) {
        userChallengeRepository.bulkUpdateCompletedChallenges(
            event.getUserChallenges().stream()
                .map(UserChallenge::getUserChallengeId)
                .toList()
        );

        event.getUserChallenges().forEach(userChallenge -> {
            ChallengeStage currentStage = userChallenge.getChallengeStage();
            stageRepository.findByChallengeIdAndStageIsGreaterThan(
                currentStage.getChallengeId(), currentStage.getStageNumber()
            ).ifPresent(nextStage ->
                userChallengeRepository.save(new UserChallenge(userChallenge, nextStage))
            );
        });
    }
}
