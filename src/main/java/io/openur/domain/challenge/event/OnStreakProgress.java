package io.openur.domain.challenge.event;

import io.openur.domain.userchallenge.model.UserChallenge;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 연속 러닝 과제 진행 이벤트.
 *
 * OnRaise/OnEvolution 과 분리한 이유는 갱신 방식이 다르기 때문이다. 두 이벤트는
 * 카운트를 1 증가시키지만, 연속 과제는 완주 이력에서 다시 계산한 값을 그대로
 * 덮어써야 한다. 연속이 끊기면 값이 내려가야 하므로 증분으로는 표현할 수 없다.
 *
 * streakLength 는 재계산된 현재 연속일이다.
 */
@Getter
@RequiredArgsConstructor
public class OnStreakProgress {

    private final UserChallenge userChallenge;
    private final int streakLength;
    private final boolean completing;
}
