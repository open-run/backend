package io.openur.domain.challenge.enums;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 유저 활동과 그 활동으로 진행되는 도전과제 ID 매핑.
 * 과제 스펙이 코드 배포 없이 바뀌는 구조가 되면 tb_challenges 컬럼으로 이전을 검토한다.
 *
 * 완주(BUNG_COMPLETE)는 벙주는 벙 완료 시점, 벙원은 완료된 벙에 첫 피드백을
 * 제출한 시점에 카운트한다. 참여 인증 여부는 조건이 아니다.
 *
 * WEEKEND_RUN(토·일)과 DAWN_RUN(04:00~06:59)은 완주 시점에 벙 시작 시각으로
 * 함께 판정한다. 토요일 새벽 벙처럼 두 조건을 동시에 만족하면 양쪽 다 오른다.
 */
@Getter
@RequiredArgsConstructor
public enum ChallengeActivityType {
    BUNG_CREATE(List.of(2L, 6L)),
    BUNG_JOIN(List.of(3L, 5L)),
    BUNG_CERTIFY(List.of(4L)),
    BUNG_COMPLETE(List.of(7L, 19L)),
    PROFILE_COMPLETE(List.of(8L)),
    WEEKEND_RUN(List.of(16L)),
    DAWN_RUN(List.of(17L));

    private final List<Long> challengeIds;
}
