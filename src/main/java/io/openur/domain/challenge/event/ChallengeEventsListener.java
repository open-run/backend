package io.openur.domain.challenge.event;

import io.openur.domain.challenge.dto.GeneralChallengeDto.OnEvolution;
import io.openur.domain.challenge.dto.GeneralChallengeDto.OnIssue;
import io.openur.domain.challenge.dto.GeneralChallengeDto.OnRaise;
import io.openur.domain.challenge.dto.GeneralChallengeDto.OnUserRegistration;
import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.challenge.repository.ChallengeStageRepository;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeEventsListener {
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeStageRepository stageRepository;
    private final ChallengeEventsPublisher eventsPublisher;


    private void airdropNFT(UserChallenge userChallenge) {
        // TODO: NFT 에어드랍
        userChallenge.setNftCompleted(true);
        userChallenge.setCompletedDate(LocalDateTime.now());
    }

//    @Transactional
//    @EventListener
//    public void handleOnCountChallengeEvent(OnRaise event) {
//        List<Long> userChallengeIds = event.getUserChallenges().stream()
//            .map(UserChallenge::getUserChallengeId)
//            .toList();
//
//        // First, increment all counts in one query
//        userChallengeRepository.bulkIncrementCount(userChallengeIds);
//
//        // Then process NFT airdrops for completed challenges
//        List<Long> completedUserChallengeIds = new ArrayList<>();
//        for (UserChallenge userChallenge : event.getUserChallenges()) {
//            Integer countCondition = userChallenge.getChallengeStage().getConditionAsCount();
//            Integer currentCount = userChallenge.getCurrentCount() + 1; // Add 1 since we just incremented
//
//            if (currentCount.equals(countCondition)) {
//                airdropNFT(userChallenge);
//                completedUserChallengeIds.add(userChallenge.getUserChallengeId());
//            }
//        }
//
//        userChallengeRepository.bulkUpdateCompletedChallenges(completedUserChallengeIds);
//    }

    // 이전의 벙 완료 트랜잭션이 반영되고 실행되어야함
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void simpleCountChallengeRaiser(OnRaise event) {
        userChallengeRepository.bulkIncrementCount(
            event.getUserChallenges()
                .stream()
                .map(UserChallenge::getUserChallengeId)
                .toList()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void simpleCountChallengeFinisher(OnEvolution event) {
        userChallengeRepository.bulkUpdateCompletedChallenges(
            event.getUserChallenges()
                .stream()
                .map(UserChallenge::getUserChallengeId)
                .toList()
        );

        // 동일 challenge 에 해당하는, 다음 스테이지의 객체를 일괄 발급해야함
        // 동시 달성시 각각의 event 가 일일히 조회를 하면서 도는 것보다는 스레드에 나눠주는게
        event.getUserChallenges().forEach(eventsPublisher::simpleChallengeIssue);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void simpleCountChallengeIssuer(OnIssue issue) {
        UserChallenge userChallenge = issue.getUserChallenge();
        ChallengeStage challengeStage = userChallenge.getChallengeStage();

        Optional<ChallengeStage> optionalNextStage =
            stageRepository.findByChallengeIdAndStageIsGreaterThan(
                challengeStage.getChallengeId(), challengeStage.getStageNumber()
            );

        if(optionalNextStage.isEmpty()) return;
        ChallengeStage nextStage = optionalNextStage.get();

        UserChallenge newUserChallenge = new UserChallenge(
            userChallenge, nextStage
        );

        userChallengeRepository.save(newUserChallenge);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewUserRegistration(OnUserRegistration userRegistration) {
        Pageable pageable = PageRequest.of(0, 100);

        Page<ChallengeStage> challengeStages;
        List<UserChallenge> newUserChallenges = new ArrayList<>();

        do {
            challengeStages = stageRepository.findAllByMinimumStages(pageable);

            challengeStages.forEach(
                stage -> newUserChallenges.add(
                    new UserChallenge(userRegistration.getUser(), stage)
                )
            );

            pageable = pageable.next();
        } while (challengeStages.hasNext());

        userChallengeRepository.bulkInsertUserChallenges(newUserChallenges);
    }

//    @Async
//    @Transactional
//    @EventListener
//    public void handleOnDateChallengeEvent(GeneralChallengeDto.OnDate event) {
//        List<Long> completedUserChallengeIds = new ArrayList<>();
//
//        for (UserChallenge userChallenge : event.getUserChallenges()) {
//            Challenge challenge = userChallenge.getChallengeStage().getChallenge();
//            LocalDateTime dateCondition = challenge.getCompletedConditionDate();
//            // TODO: Implement proper date condition check
//            if (dateCondition != null && LocalDateTime.now().isAfter(dateCondition)) {
//                airdropNFT(userChallenge);
//                completedUserChallengeIds.add(userChallenge.getUserChallengeId());
//            }
//        }
//
//        userChallengeRepository.bulkUpdateCompletedChallenges(completedUserChallengeIds);
//    }
//
//    @Async
//    @Transactional
//    @EventListener
//    public void handleOnPlaceChallengeEvent(GeneralChallengeDto.OnPlace event) {
//        List<Long> completedUserChallengeIds = new ArrayList<>();
//
//        for (UserChallenge userChallenge : event.getUserChallenges()) {
//            ChallengeStage challengeStage = userChallenge.getChallengeStage();
//            Integer countCondition = challengeStage.getConditionAsCount();
//            String placeCondition = challengeStage.getChallenge().getCompletedConditionText();
//            // TODO: Implement proper place condition check
//            if (placeCondition != null && placeCondition.equals(event.getLocation())) {
//                airdropNFT(userChallenge);
//                completedUserChallengeIds.add(userChallenge.getUserChallengeId());
//            }
//        }
//
//        userChallengeRepository.bulkUpdateCompletedChallenges(completedUserChallengeIds);
//    }

//    @Async
//    @Transactional
//    @EventListener
//    public void handleOnWearingChallengeEvent(GeneralChallengeDto.OnWearing event) {
//        List<Long> completedUserChallengeIds = new ArrayList<>();
//
//        for (UserChallenge userChallenge : event.getUserChallenges()) {
//            Challenge challenge = userChallenge.getChallengeStage();
////            Boolean wearingCondition = challenge.getCompletedConditionText();
////            // TODO: 옷 정보 불/합치 정보는 NFT 완성 이후 논의 필요
////            if (wearingCondition != null && wearingCondition) {
////                airdropNFT(userChallenge);
////                completedUserChallengeIds.add(userChallenge.getUserChallengeId());
////            }
//        }
//
//        userChallengeRepository.bulkUpdateCompletedChallenges(completedUserChallengeIds);
//    }
}

