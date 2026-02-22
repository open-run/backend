package io.openur.domain.challenge.event;

import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.challenge.repository.ChallengeStageRepository;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChallengeEventsListener {
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeStageRepository stageRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeRaise(OnRaise event) {
        userChallengeRepository.bulkIncrementCount(
            event.getUserChallenges().stream()
                .map(UserChallenge::getUserChallengeId)
                .toList()
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistration(OnUserRegistration event) {
        Pageable pageable = PageRequest.of(0, 100);
        List<UserChallenge> newUserChallenges = new ArrayList<>();
        Page<ChallengeStage> challengeStages;

        do {
            challengeStages = stageRepository.findAllByMinimumStages(pageable);
            challengeStages.forEach(stage -> 
                newUserChallenges.add(new UserChallenge(event.getUser(), stage))
            );
            pageable = pageable.next();
        } while (challengeStages.hasNext());

        userChallengeRepository.bulkInsertUserChallenges(newUserChallenges);
    }
}

