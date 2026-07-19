package io.openur.domain.challenge.event;

import io.openur.domain.challenge.enums.ChallengeActivityType;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import io.openur.domain.userchallenge.service.UserChallengeInitializer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChallengeEventsPublisher {

    private final ApplicationEventPublisher publisher;
    private final UserChallengeRepository userChallengeRepository;
    private final UserChallengeInitializer userChallengeInitializer;

    @Transactional
    public void publishChallengeCheck(String userId, ChallengeActivityType activity) {
        userChallengeInitializer.ensureInitialized(userId);

        List<UserChallenge> targets =
            userChallengeRepository.findAllUncompletedByChallengeIds(
                userId, activity.getChallengeIds());
        if (targets.isEmpty()) return;

        List<UserChallenge> toRaise = targets.stream()
            .filter(uc -> uc.getCurrentCount() + 1 < uc.getChallengeStage().getConditionAsCount())
            .toList();
        List<UserChallenge> toEvolve = targets.stream()
            .filter(uc -> uc.getCurrentCount() + 1 >= uc.getChallengeStage().getConditionAsCount())
            .toList();

        if (!toRaise.isEmpty()) publisher.publishEvent(new OnRaise(toRaise));
        if (!toEvolve.isEmpty()) publisher.publishEvent(new OnEvolution(toEvolve));
    }
}
