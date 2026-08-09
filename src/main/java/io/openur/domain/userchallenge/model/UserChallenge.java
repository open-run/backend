package io.openur.domain.userchallenge.model;

import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.user.model.User;
import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class UserChallenge {

    private Long userChallengeId;
    private User user;
    private ChallengeStage challengeStage;
    @Setter
    private LocalDateTime completedDate;
    @Setter
    private Boolean nftCompleted;  // TODO: 단순 boolean 말고 NFT index 정보를 갖게 하는것도?
    @Setter
    private Integer currentCount;
    @Setter
    private Float currentProgress;

    public UserChallenge(UserChallenge prevChallenge, ChallengeStage nextStage
    ) {
        this.user = prevChallenge.getUser();
        this.challengeStage = nextStage;
        this.completedDate = null;
        this.nftCompleted = false;
        // 단계 조건은 누적 회수 기준. prevChallenge는 완료 직전에 조회된 스냅샷이라
        // 완료 처리에서 더해진 +1이 빠져 있으므로 여기서 보정해 승계한다.
        this.currentCount = prevChallenge.getCurrentCount() + 1;
        this.currentProgress = toProgressPercent(this.currentCount, nextStage.getConditionAsCount());
    }

    /**
     * 연속 러닝 과제의 다음 단계를 만든다.
     *
     * 일반 승계(위 생성자)는 스냅샷 카운트에 1을 더하지만, 연속 과제는 그러면 안 된다.
     * 연속이 끊긴 직후 완료되는 경우 실제 연속일은 1인데 끊기기 전 누적값을 물려받게 된다.
     * 재계산된 연속일을 명시적으로 받아 그대로 승계한다.
     */
    public static UserChallenge nextStreakStage(
        UserChallenge prevChallenge, ChallengeStage nextStage, int streakLength
    ) {
        UserChallenge next = new UserChallenge(prevChallenge.getUser(), nextStage);
        next.setCurrentCount(streakLength);
        next.setCurrentProgress(
            toProgressPercent(streakLength, nextStage.getConditionAsCount()));
        return next;
    }

    /**
     * 진행률은 퍼센트(0~100)로 표기한다. currentProgress 는 가공 없이 응답에 실리므로
     * 저장 시점과 조회 시점의 스케일이 반드시 같아야 한다.
     */
    public static float toProgressPercent(int currentCount, int conditionAsCount) {
        if (conditionAsCount <= 0) {
            return 0.0f;
        }
        return (float) currentCount / conditionAsCount * 100;
    }

    public UserChallenge(User user, ChallengeStage stage) {
        this.user = user;
        this.challengeStage = stage;
        this.completedDate = null;
        this.nftCompleted = false;
        this.currentCount = 0;
        this.currentProgress = 0.0f;
    }

    public static UserChallenge from(final UserChallengeEntity userChallengeEntity) {
        return new UserChallenge(
            userChallengeEntity.getUserChallengeId(),
            User.from(userChallengeEntity.getUserEntity()),
            ChallengeStage.from(userChallengeEntity.getChallengeStageEntity()),
            userChallengeEntity.getCompletedDate(),
            userChallengeEntity.getNftCompleted(),
            userChallengeEntity.getCurrentCount(),
            userChallengeEntity.getCurrentProgress()
        );
    }

    public UserChallengeEntity toEntity() {
        return new UserChallengeEntity(
            this.userChallengeId,
            this.user.toEntity(),
            this.challengeStage.toEntity(),
            this.currentCount,
            this.currentProgress,
            this.completedDate,
            this.nftCompleted
        );
    }

    public void raiseCount() {
        this.currentCount++;
    }

    public void completeChallenge() {
        this.currentCount++;
        this.completedDate = LocalDateTime.now();
    }
}
