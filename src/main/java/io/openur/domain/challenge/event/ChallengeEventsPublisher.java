package io.openur.domain.challenge.event;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.model.User;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeEventsPublisher {

    private final ApplicationEventPublisher publisher;
    private final UserChallengeRepository userChallengeRepository;

    public void publishBungCompletion(Bung bung, String userId) {
        publishChallengeCheck(userId);
    }

    public void publishChallengeCheck(String userId) {
        userChallengeRepository.findFirstBySimpleRepetitiveChallenge(userId)
            .ifPresent(userChallenge -> {
                if(userChallenge.getCurrentCount() + 1 < userChallenge.getChallengeStage().getConditionAsCount())
                    publisher.publishEvent(new OnRaise(List.of(userChallenge)));
                else
                    publisher.publishEvent(new OnEvolution(List.of(userChallenge)));
            });
    }

    public void publishChallengeIssue(UserChallenge userChallenge) {
        publisher.publishEvent(new OnIssue(userChallenge));
    }

    public void publishUserRegistration(User user) {
        publisher.publishEvent(new OnUserRegistration(user));
    }
}
