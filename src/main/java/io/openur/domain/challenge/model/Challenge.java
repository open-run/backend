package io.openur.domain.challenge.model;
import io.openur.domain.challenge.entity.ChallengeEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class Challenge {
    private Long challengeId;
    private String challengeName;
    private ChallengeType challengeType;
    private String description;
    private RewardType rewardType;
    private BigDecimal rewardPercentage;
    private CompletedType completedType;
    private Long completedConditionCount;
    private LocalDateTime completedConditionDate;
    private String completedConditionPlace;
    private Boolean completedConditionWearing;
    public static Challenge from(final ChallengeEntity challengeEntity) {
        return new Challenge(
            challengeEntity.getChallengeId(),
            challengeEntity.getChallengeName(),
            challengeEntity.getChallengeType(),
            challengeEntity.getDescription(),
            challengeEntity.getRewardType(),
            challengeEntity.getRewardPercentage(),
            challengeEntity.getCompletedType(),
            challengeEntity.getCompletedConditionCount(),
            challengeEntity.getCompletedConditionDate(),
            challengeEntity.getCompletedConditionPlace(),
            challengeEntity.getCompletedConditionWearing()
        );
    }
    public ChallengeEntity toEntity() {
        return new ChallengeEntity(
            this.challengeId,
            this.challengeName,
            this.challengeType,
            this.description,
            this.rewardType,
            this.rewardPercentage,
            this.completedType,
            this.completedConditionCount,
            this.completedConditionDate,
            this.completedConditionPlace,
            this.completedConditionWearing
        );
    }
}
