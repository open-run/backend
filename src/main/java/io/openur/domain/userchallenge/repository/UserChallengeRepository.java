package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.dto.ChallengeRow;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface UserChallengeRepository {

    UserChallenge save(UserChallenge userChallenge);

    @Transactional
    void bulkInsertUserChallenges(List<UserChallenge> userChallenges);

    /**
     * Increments the currentCount for challenges of type COUNT
     * @param userChallengeIds List of user challenge IDs to update
     */
    void bulkIncrementCount(List<Long> userChallengeIds);

    /**
     * Marks challenges as achieved and ready for NFT reward issuance.
     * 이미 완료된 행은 건너뛰므로, 동시 요청 중 실제로 전환에 성공한 쪽만 결과에 담긴다.
     * 호출자는 반환된 ID에 대해서만 다음 단계를 발급해야 중복 발급을 피할 수 있다.
     *
     * @param completedUserChallengeIds List of challenges that reached their completion condition
     * @return 이 호출이 실제로 완료 처리한 user_challenge ID 목록
     */
    List<Long> markCompletedIfPending(List<Long> completedUserChallengeIds);

    /**
     * 연속 러닝 과제 진행 갱신. 재계산된 연속일을 그대로 덮어쓴다(리셋 포함).
     * completing 이면 완료 처리까지 함께 수행한다.
     *
     * @return 이 호출이 실제로 갱신했으면 true. 이미 완료된 행이면 false
     */
    boolean updateStreakProgress(
        Long userChallengeId, int streakLength, float progressPercent, boolean completing
    );

    void markNftCompleted(Long userChallengeId);

    Set<Long> findChallengeIdsByUserId(String userId);

    Page<ChallengeRow> findUncompletedChallengesByUserId(
        String userId, Pageable pageable
    );

    Page<UserChallenge> findCompletedChallengesByUserId(
        String userId, Pageable pageable
    );

    Page<UserChallenge> findCompletedAndNftIssuedChallengesByUserId(
        String userId, Pageable pageable
    );

    Page<ChallengeRow> findRepetitiveChallengesByUserId(
        String userId, Pageable pageable
    );

    Map<Long, UserChallenge> findRepetitiveUserChallengesMappedByStageId(
        String userId, Long challengeId
    );

    List<UserChallenge> findAllUncompletedByChallengeIds(String userId, List<Long> challengeIds);

    void delete(UserChallenge userChallenge);
}
