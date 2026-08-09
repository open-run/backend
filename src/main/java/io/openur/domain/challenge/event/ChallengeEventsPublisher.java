package io.openur.domain.challenge.event;

import io.openur.domain.challenge.enums.ChallengeActivityType;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import io.openur.domain.userchallenge.service.UserChallengeInitializer;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChallengeEventsPublisher {

    // 새벽 러너 판정 구간: 04:00 ~ 06:59
    private static final int DAWN_START_HOUR = 4;
    private static final int DAWN_END_HOUR = 7;

    private final ApplicationEventPublisher publisher;
    private final UserChallengeRepository userChallengeRepository;
    private final UserChallengeInitializer userChallengeInitializer;

    @Transactional
    public void publishChallengeCheck(String userId, ChallengeActivityType activity) {
        userChallengeInitializer.ensureInitialized(userId);
        raiseOrEvolve(userId, activity);
    }

    /**
     * 완주 시점 일괄 판정. 기본 완주 과제에 더해, 벙이 열린 시각으로 판정하는
     * 주말·새벽 과제까지 함께 진행시킨다.
     *
     * 판정 기준을 완주 처리 시각이 아니라 벙 시작 시각으로 두는 이유는, 벙주는 완료
     * 버튼을 누른 시점에 벙원은 첫 피드백을 낸 시점에 이 경로를 타기 때문이다.
     * 같은 벙이면 누가 언제 완주 처리하든 같은 날짜·시간대로 판정되어야 한다.
     *
     * bungStartDateTime 은 타임존 없는 벽시계 값이라 서버 타임존에 영향을 받지 않는다.
     */
    @Transactional
    public void publishBungCompleteChecks(String userId, LocalDateTime bungStartDateTime) {
        userChallengeInitializer.ensureInitialized(userId);

        raiseOrEvolve(userId, ChallengeActivityType.BUNG_COMPLETE);

        DayOfWeek dayOfWeek = bungStartDateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            raiseOrEvolve(userId, ChallengeActivityType.WEEKEND_RUN);
        }

        int hour = bungStartDateTime.getHour();
        if (hour >= DAWN_START_HOUR && hour < DAWN_END_HOUR) {
            raiseOrEvolve(userId, ChallengeActivityType.DAWN_RUN);
        }
    }

    private void raiseOrEvolve(String userId, ChallengeActivityType activity) {
        List<UserChallenge> targets =
            userChallengeRepository.findAllUncompletedByChallengeIds(
                userId, activity.getChallengeIds());
        if (targets.isEmpty()) return;

        List<UserChallenge> toRaise = targets.stream()
            .filter(uc -> uc.getCurrentCount() + 1 < uc.getChallengeStage().getConditionAsCount())
            .toList();
        List<UserChallenge> toEvolve = targets.stream()
            .filter(uc -> uc.getCurrentCount() + 1 >= uc.getChallengeStage().getConditionAsCount())
            .toList();

        if (!toRaise.isEmpty()) publisher.publishEvent(new OnRaise(toRaise));
        if (!toEvolve.isEmpty()) publisher.publishEvent(new OnEvolution(toEvolve));
    }
}
