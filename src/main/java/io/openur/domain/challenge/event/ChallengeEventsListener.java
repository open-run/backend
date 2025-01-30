package io.openur.domain.challenge.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;

@Component
public class ChallengeEventsListener {
    @Async
    @Transactional
    @EventListener
    public void handleOnDateChallengeEvent(OnDate event) {
        // TODO
    }

    @Async
    @Transactional
    @EventListener
    public void handleOnCountChallengeEvent(OnCount event) {
        // TODO 
    }

    @Async
    @Transactional
    @EventListener
    public void handleOnPlaceChallengeEvent(OnPlace event) {
        // TODO
    }

    @Async
    @Transactional
    @EventListener
    public void handleOnWearingChallengeEvent(OnWearing event) {
        // TODO
    }
}
