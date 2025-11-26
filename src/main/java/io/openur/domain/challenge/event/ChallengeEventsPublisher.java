package io.openur.domain.challenge.event;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.challenge.dto.GeneralChallengeDto;
import io.openur.domain.challenge.dto.GeneralChallengeDto.OnEvolution;
import io.openur.domain.challenge.dto.GeneralChallengeDto.OnRaise;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import java.util.List;
import java.util.Optional;
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
     * @param userId List of user IDs who participated in the Bung
     * 
     * TODO: 
     * - Implement filtering of challenge IDs that must be triggered when bung is complete
     * - Add initial UserChallenge data creation process
     * - Implement user NFT ownership indexing and current wearing data handling
     * 메소드 분리
     *                groupingBy -> groupBy Query 로 통합
     *                queryDsl 의 경우 id 로 검색하는
     */
    // 설계 한계가 존재함, 오히려 한번에 처리할 것이 아니라 참여한 인원별로 각자의 event 를 발생시키는 것이 맞는거 같다.
//    public void bungIsComplete(Bung bung, List<String> userId) {
//        // TODO: 벙이 완료됐을 떄 트리거 되어야 하는 challengeID 만 필터링 필요.
//        // Filter out completed challenges and group by completion type
//        Map<CompletedType, List<UserChallenge>> challengesByType = userChallengeRepository
//            .findByUserIdsAndChallengeIdsGroupByCompletedType(userId);
//
//        // Publish events for each group
//        if (challengesByType.containsKey(CompletedType.date)) {
//            publisher.publishEvent(new GeneralChallengeDto.OnDate(challengesByType.get(CompletedType.date)));
//        }
//
//        publisher.publishEvent(new GeneralChallengeDto.OnRaise(challengesByType.get(CompletedType.count)));
//
//        if (challengesByType.containsKey(CompletedType.place)) {
//            publisher.publishEvent(new GeneralChallengeDto.OnPlace(challengesByType.get(CompletedType.place), bung.getLocation()));
//        }
//
//        if (challengesByType.containsKey(CompletedType.wearing)) {
//            // TODO: 유저 소유 NFT 정보 인덱싱 및 현재 입고있는 데이터 핸들링 후 가져오기.
//            List<UserChallenge> wearingChallenges = challengesByType.get(CompletedType.wearing);
//            if (!wearingChallenges.isEmpty()) {
//                publisher.publishEvent(new GeneralChallengeDto.OnWearing(wearingChallenges,
//                    wearingChallenges.get(0).getUser().getBlockchainAddress()));
//            }
//        }
//    }
    public void bungIsCompleted(Bung bung, String userId) {
        simpleChallengeCheck(userId);

    }

    public void simpleChallengeCheck(String userId) {
        // **Challenge 구조
        // UserChallenge 는 ChallengeStage 와 연결되어 만에 하나 반복되는 경우 혹은 반복횟수 추가 건에 대해 반응할 수 있도록 설계
        // Challenge 는 공통적으로 주어지는 완수 조건에 해당 ( 예 : 의상 혹은 위치 값, *횟수는 포함되지 않는다 )
        // 만에 하나, 반복 스테이지가 올라갈때마다 달성 보상 등급 또는 파츠를 다르게 주고 싶다면, RewardType 또는 CompletedType 을 개별적으로 주어줄 필요가 있다.
        // 따라서 완료 처리를 할 대상은, ChallengeStage 의 값과 비교가 된 UserChallenge 에 해당하며, 해당 미션 완수시에 다음 스테이지가 있다면 다음 스테이지로의 엔티티 생성까지 연결되어야한다.
        Optional<UserChallenge> optionalUserChallenge = userChallengeRepository.findBySimpleRepetitiveChallenge(userId);
        if(optionalUserChallenge.isEmpty()) return;

        UserChallenge userChallenge = optionalUserChallenge.get();
        if(userChallenge.getCurrentCount() + 1 < userChallenge.getChallengeStage().getConditionAsCount())
            publisher.publishEvent(new OnRaise(List.of(userChallenge)));
        else
            publisher.publishEvent(new OnEvolution(userChallenge, true));
        // 완료 달성 체크를 할 대상은, bung date 와 condition text ( 아마도 like 검색, 위치, 의상 등은 아직 잘 모름 ) 을 조건으로 내걸고,
        // Stage 값이 제일 낮으며 완수되지 않은 건으로 검색을 시도해서, count 조작을 시도한다. ** 현재로선 bung 에 조건 텍스트 필드가 없어 미구현
    }
}
