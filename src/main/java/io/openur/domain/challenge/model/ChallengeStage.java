package io.openur.domain.challenge.model;

import io.openur.domain.challenge.entity.ChallengeStageEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChallengeStage {
    private Long stageId;
    private Integer stageNumber;
    private Integer conditionAsCount;
    private Long challengeId;

    public static ChallengeStage from(ChallengeStageEntity challengeStageEntity) {
        return new ChallengeStage(
            challengeStageEntity.getStageId(),
            challengeStageEntity.getStageNumber(),
            challengeStageEntity.getConditionAsCount(),
            challengeStageEntity.getChallengeEntity().getChallengeId()
        );
    }

    public ChallengeStageEntity toEntity(Challenge challenge) {
        return new ChallengeStageEntity(
            this.stageId,
            this.stageNumber,
            this.conditionAsCount,
            challenge.toEntity()
        );
    }

    public ChallengeStageEntity toEntity() {
        return new ChallengeStageEntity(
            this.stageId,
            this.stageNumber,
            this.conditionAsCount,
            null
        );
    }
}
