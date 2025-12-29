package io.openur.domain.challenge.model;

import io.openur.domain.challenge.entity.ChallengeEntity;
import io.openur.domain.challenge.entity.ChallengeStageEntity;
import io.openur.domain.challenge.enums.ChallengeType;
import io.openur.domain.challenge.enums.CompletedType;
import io.openur.domain.challenge.enums.RewardType;
import java.time.LocalDateTime;
import java.util.List;
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
    private CompletedType completedType;
    private LocalDateTime completedConditionDate;
    private String completedConditionText;
//    private List<ChallengeStage> challengeStages;

    public ChallengeEntity toEntity(List<ChallengeStage> challengeStages) {
        return new ChallengeEntity(
            this.challengeId,
            this.challengeName,
            this.challengeDescription,
            this.challengeType,
            this.rewardType,
            this.completedType,
            this.completedConditionDate,
            this.completedConditionText,
            challengeStages.stream().map(ChallengeStage::toEntity).toList()
        );
    }

    public ChallengeEntity toEntity() {
        return new ChallengeEntity(
            this.challengeId,
            this.challengeName,
            this.challengeDescription,
            this.challengeType,
            this.rewardType,
            this.completedType,
            this.completedConditionDate,
            this.completedConditionText,
            null
        );
    }
    
    public static Challenge from(final ChallengeEntity challengeEntity) {
        return new Challenge(
            challengeEntity.getChallengeId(),
            challengeEntity.getName(),
            challengeEntity.getDescription(),
            challengeEntity.getChallengeType(),
            challengeEntity.getRewardType(),
            challengeEntity.getCompletedType(),
//            challengeEntity.getConditionAsCount(),
            challengeEntity.getConditionAsDate(),
            challengeEntity.getConditionAsText()
        );
    }
}
