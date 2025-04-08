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
