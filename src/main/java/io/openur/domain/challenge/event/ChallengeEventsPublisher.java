package io.openur.domain.challenge.event;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.challenge.model.CompletedType;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeEventsPublisher {

    private final ApplicationEventPublisher publisher;
    private final UserChallengeRepository userChallengeRepository;

    /**
     * Processes challenge completion events when a Bung is completed.
     * This method evaluates different types of challenges (date, count, place, wearing) 
     * and publishes corresponding events for eligible user challenges.
     * 
     * The method performs the following steps:
     * 1. Filters relevant challenge IDs for the completed Bung
     * 2. Retrieves active user challenges for the participating users
     * 3. Groups challenges by completion type (date, count, place, wearing)
     * 4. Publishes appropriate events for each challenge type
     * 
     * @param bung The completed Bung containing information about the gathering
     * @param userIds List of user IDs who participated in the Bung
     * 
     * TODO: 
     * - Implement filtering of challenge IDs that must be triggered when bung is complete
     * - Add initial UserChallenge data creation process
     * - Implement user NFT ownership indexing and current wearing data handling
     * 메소드 분리
     *                groupingBy -> groupBy Query 로 통합
     *                queryDsl 의 경우 id 로 검색하는
     */
    public void bungIsComplete(Bung bung, List<String> userIds) {
        // TODO: 벙이 완료됐을 떄 트리거 되어야 하는 challengeID 만 필터링 필요. 
        List<Long> challengeIds = List.of(1L, 2L, 3L);

        // Filter out completed challenges and group by completion type
        Map<CompletedType, List<UserChallenge>> challengesByType = userChallengeRepository
            .findByUserIdsAndChallengeIdsGroupByCompletedType(userIds, challengeIds);

        // Publish events for each group
        if (challengesByType.containsKey(CompletedType.date)) {
            publisher.publishEvent(new OnDate(challengesByType.get(CompletedType.date)));
        }

        if (challengesByType.containsKey(CompletedType.count)) {
            publisher.publishEvent(new OnCount(challengesByType.get(CompletedType.count)));
        }

        if (challengesByType.containsKey(CompletedType.place)) {
            publisher.publishEvent(new OnPlace(challengesByType.get(CompletedType.place), bung.getLocation()));
        }

        if (challengesByType.containsKey(CompletedType.wearing)) {
            // TODO: 유저 소유 NFT 정보 인덱싱 및 현재 입고있는 데이터 핸들링 후 가져오기.
            List<UserChallenge> wearingChallenges = challengesByType.get(CompletedType.wearing);
            if (!wearingChallenges.isEmpty()) {
                publisher.publishEvent(new OnWearing(wearingChallenges,
                    wearingChallenges.get(0).getUser().getBlockchainAddress()));
            }
        }
    }
}
