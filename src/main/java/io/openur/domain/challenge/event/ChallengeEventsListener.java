package io.openur.domain.challenge.event;

import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.challenge.repository.ChallengeStageRepository;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final ChallengeEventsPublisher eventsPublisher;

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

        event.getUserChallenges().forEach(eventsPublisher::publishChallengeIssue);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeIssue(OnIssue issue) {
        UserChallenge userChallenge = issue.getUserChallenge();
        ChallengeStage challengeStage = userChallenge.getChallengeStage();

        Optional<ChallengeStage> optionalNextStage = stageRepository
            .findByChallengeIdAndStageIsGreaterThan(
                challengeStage.getChallengeId(), challengeStage.getStageNumber()
            );

        if(optionalNextStage.isEmpty()) return;

        userChallengeRepository.save(
            new UserChallenge(userChallenge, optionalNextStage.get())
        );
    }

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

