package io.openur.domain.challenge.events;

import io.openur.domain.challenge.dto.ChallengeCompletionDto;
import io.openur.domain.challenge.dto.ChallengeCompletionDto.ofCount;
import io.openur.domain.challenge.dto.ChallengeCompletionDto.ofDate;
import io.openur.domain.challenge.dto.ChallengeCompletionDto.ofLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ChallengeEventsPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishChallengeCheck(ChallengeCompletionDto dto) {
        checkCountChallenge(dto);
        checkDateChallenge(dto);
        checkLocationChallenge(dto);

        if(!StringUtils.hasText(dto.getOutfitInfos())) checkOutfitChallenge(dto);
    }

    private void checkDateChallenge(ChallengeCompletionDto dto) {
        eventPublisher.publishEvent(new ofDate(dto));
    }

    private void checkCountChallenge(ChallengeCompletionDto dto) {
        eventPublisher.publishEvent(new ofCount(dto));
    }

    private void checkLocationChallenge(ChallengeCompletionDto dto) {
        eventPublisher.publishEvent(new ofLocation(dto));
    }

    private void checkOutfitChallenge(ChallengeCompletionDto dto) {
        eventPublisher.publishEvent(new ofCount(dto));
    }
}
