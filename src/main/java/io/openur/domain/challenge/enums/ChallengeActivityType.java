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
 */
@Getter
@RequiredArgsConstructor
public enum ChallengeActivityType {
    BUNG_CREATE(List.of(2L, 6L)),
    BUNG_JOIN(List.of(3L, 5L)),
    BUNG_CERTIFY(List.of(4L)),
    BUNG_COMPLETE(List.of(7L, 19L)),
    // TODO: 프로필 완성 API에 배선 예정
    PROFILE_COMPLETE(List.of(8L));

    private final List<Long> challengeIds;
}
