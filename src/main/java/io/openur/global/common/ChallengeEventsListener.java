package io.openur.global.common;

import io.openur.domain.challenge.dto.ChallengeCompletionDto.ofDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeEventsListener {

    @Async
    @EventListener
    public void onDateChallengeCheck(ofDate event) {
        System.out.println("Date Challenge Check");
    }

    @Async
    @EventListener
    public void onCountChallengeCheck(ofDate event) {
        System.out.println("Count Challenge Check");
    }

    @Async
    @EventListener
    public void onLocationChallengeCheck(ofDate event) {
        System.out.println("Location Challenge Check");
    }

    @Async
    @EventListener
    public void onOutfitChallengeCheck(ofDate event) {
        System.out.println("Outfit Challenge Check");
    }
}
