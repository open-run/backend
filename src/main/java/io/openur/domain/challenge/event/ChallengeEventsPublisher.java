package io.openur.domain.challenge.event;

import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import io.openur.domain.bung.model.Bung;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengeEventsPublisher {
    private final ApplicationEventPublisher publisher;

    public void bungIsComplete(Bung bung, List<String> userIds) {
        // TODO get users' challenges that needs checking when bung is completed
        // and publish to appropriate event
    }

    private void checkOnDate(OnDate event) {
        publisher.publishEvent(event);
    }

    private void checkOnCount(OnCount event) {
        publisher.publishEvent(event);
    }

    private void checkOnPlace(OnPlace event) {
        publisher.publishEvent(event);
    }

    private void checkOnWearing(OnWearing event) {
        publisher.publishEvent(event);
    }
}
