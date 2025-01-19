package io.openur.domain.bung.event;

import io.openur.domain.bung.model.Bung;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BungEventPublishers {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void bungIsComplete(Bung bung, List<String> userIds) {
        BungIsCompleteEvent event = new BungIsCompleteEvent(this, bung, userIds);
        applicationEventPublisher.publishEvent(event);
    }


}