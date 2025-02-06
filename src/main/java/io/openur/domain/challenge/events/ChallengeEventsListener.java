package io.openur.domain.challenge.events;

import io.openur.domain.challenge.dto.ChallengeCompletionDto.ofCount;
import io.openur.domain.challenge.dto.ChallengeCompletionDto.ofDate;
import io.openur.domain.challenge.dto.ChallengeCompletionDto.ofLocation;
import io.openur.domain.challenge.dto.ChallengeCompletionDto.ofOutfit;
import io.openur.domain.userchallenge.repository.UserChallengeRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeEventsListener {

    private final UserChallengeRepositoryImpl userChallengeRepositoryImpl;

    @Async
    @EventListener
    public void onDateChallengeCheck(ofDate event) {
        System.out.println("Date Challenge Check");
    }

    @Async
    @EventListener
    public void onCountChallengeCheck(ofCount event) {
        userChallengeRepositoryImpl.bulkUpdateChallengeProgress(event.getUserIds());

        // TODO: 만약 해당 쿼리가 실행되고 나서, 달성된 Challenges 의 오너들이 NFT를 받아야하면, 추가코드 필요
        System.out.println("Count Challenge Check");
    }

    @Async
    @EventListener
    public void onLocationChallengeCheck(ofLocation event) {
        System.out.println("Location Challenge Check");
    }

    @Async
    @EventListener
    public void onOutfitChallengeCheck(ofOutfit event) {
        System.out.println("Outfit Challenge Check");
    }
}
