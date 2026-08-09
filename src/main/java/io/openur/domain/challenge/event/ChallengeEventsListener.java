package io.openur.domain.challenge.event;

import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.challenge.repository.ChallengeStageRepository;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeEventsListener {
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeStageRepository stageRepository;

    @EventListener
    public void handleChallengeRaise(OnRaise event) {
        userChallengeRepository.bulkIncrementCount(
            event.getUserChallenges().stream()
                .map(UserChallenge::getUserChallengeId)
                .toList()
        );
    }

    @EventListener
    public void handleChallengeEvolution(OnEvolution event) {
        List<Long> transitioned = userChallengeRepository.markCompletedIfPending(
            event.getUserChallenges().stream()
                .map(UserChallenge::getUserChallengeId)
                .toList()
        );

        if (transitioned.isEmpty()) {
            return;
        }

        // 이 호출이 실제로 완료 처리한 행에 대해서만 다음 단계를 발급한다.
        // 동시 요청 둘이 같은 행을 완료로 보고 진입해도 다음 단계 행은 하나만 생긴다.
        Set<Long> transitionedIds = Set.copyOf(transitioned);
        event.getUserChallenges().stream()
            .filter(userChallenge ->
                transitionedIds.contains(userChallenge.getUserChallengeId()))
            .forEach(userChallenge -> {
                ChallengeStage currentStage = userChallenge.getChallengeStage();
                stageRepository.findByChallengeIdAndStageIsGreaterThan(
                    currentStage.getChallengeId(), currentStage.getStageNumber()
                ).ifPresent(nextStage ->
                    userChallengeRepository.save(new UserChallenge(userChallenge, nextStage))
                );
            });
    }

    @EventListener
    public void handleStreakProgress(OnStreakProgress event) {
        UserChallenge userChallenge = event.getUserChallenge();
        ChallengeStage currentStage = userChallenge.getChallengeStage();

        boolean applied = userChallengeRepository.updateStreakProgress(
            userChallenge.getUserChallengeId(),
            event.getStreakLength(),
            UserChallenge.toProgressPercent(
                event.getStreakLength(), currentStage.getConditionAsCount()),
            event.isCompleting()
        );

        if (!applied || !event.isCompleting()) {
            return;
        }

        // 다음 단계는 재계산된 연속일을 그대로 물려받는다.
        stageRepository.findByChallengeIdAndStageIsGreaterThan(
            currentStage.getChallengeId(), currentStage.getStageNumber()
        ).ifPresent(nextStage ->
            userChallengeRepository.save(
                UserChallenge.nextStreakStage(userChallenge, nextStage, event.getStreakLength()))
        );
    }
}
