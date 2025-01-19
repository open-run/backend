package io.openur.domain.challenge;

import java.util.List;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class ChallengeEvent extends ApplicationEvent {

    private final List<Long> challengeIds;

    public ChallengeEvent(Object source, List<Long> challengeIds) {
        super(source);
        this.challengeIds = challengeIds;
    }
}