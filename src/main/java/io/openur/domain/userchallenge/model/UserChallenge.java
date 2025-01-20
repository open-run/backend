package io.openur.domain.userchallenge.model;

import io.openur.domain.challenge.model.Challenge;
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
    private Challenge challenge;
    private LocalDateTime completedDate;
    @Setter
    private Boolean nftCompleted;
    @Setter
    private Long currentCount;

    public static UserChallenge from(final UserChallengeEntity userChallengeEntity) {
        return new UserChallenge(
            userChallengeEntity.getUserChallengeId(),
            User.from(userChallengeEntity.getUserEntity()),
            Challenge.from(userChallengeEntity.getChallengeEntity()),
            userChallengeEntity.getCompletedDate(),
            userChallengeEntity.getNftCompleted(),
            userChallengeEntity.getCurrentCount()
        );
    }

    public UserChallengeEntity toEntity() {
        return new UserChallengeEntity(
            this.userChallengeId,
            this.user.toEntity(),
            this.challenge.toEntity(),
            this.completedDate,
            this.nftCompleted,
            this.currentCount
        );
    }
}
