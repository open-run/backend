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

    public static UserChallenge from(final UserChallengeEntity userChallengeEntity) {
        return new UserChallenge(
            userChallengeEntity.getUserChallengeId(),
            User.from(userChallengeEntity.getUserEntity()),
            ChallengeStage.from(userChallengeEntity.getChallengeStageEntity()),
            userChallengeEntity.getCompletedDate(),
            userChallengeEntity.getNftCompleted(),
            userChallengeEntity.getCurrentCount()
        );
    }

    public UserChallengeEntity toEntity() {
        return new UserChallengeEntity(
            this.userChallengeId,
            this.user.toEntity(),
            this.challengeStage.toEntity(),
            this.currentCount,
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
