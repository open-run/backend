package io.openur.domain.challenge.model;

import io.openur.domain.challenge.entity.ChallengeEntity;
import io.openur.domain.challenge.enums.ChallengeType;
import io.openur.domain.challenge.enums.CompletedType;
import io.openur.domain.challenge.enums.RewardType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Challenge {

    private Long challengeId;
    private String challengeName;
    private String challengeDescription;
    private ChallengeType challengeType;
    private RewardType rewardType;
    private Float rewardPercentage;
    private CompletedType completedType;
    private Integer completedConditionCount;
    private LocalDateTime completedConditionDate;
    private String completedConditionText;
    
    public ChallengeEntity toEntity() {
        return new ChallengeEntity(
            this.challengeId,
            this.challengeName,
            this.challengeDescription,
            this.challengeType,
            this.rewardType,
            this.rewardPercentage,
            this.completedType,
            this.completedConditionCount,
            this.completedConditionDate,
            this.completedConditionText
        );
    }
    
    public static Challenge from(final ChallengeEntity challengeEntity) {
        return new Challenge(
            challengeEntity.getChallengeId(),
            challengeEntity.getName(),
            challengeEntity.getDescription(),
            challengeEntity.getChallengeType(),
            challengeEntity.getRewardType(),
            challengeEntity.getRewardPercentage(),
            challengeEntity.getCompletedType(),
            challengeEntity.getConditionAsCount(),
            challengeEntity.getConditionAsDate(),
            challengeEntity.getConditionAsText()
        );
    }
}
