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
    private Challenge challenge;

    public static ChallengeStage from(ChallengeStageEntity challengeStageEntity) {
        return new ChallengeStage(
            challengeStageEntity.getStageId(),
            challengeStageEntity.getStageNumber(),
            challengeStageEntity.getConditionAsCount(),
            challengeStageEntity.getChallengeEntity().getChallengeId(),
            Challenge.from(challengeStageEntity.getChallengeEntity())
        );
    }

    public ChallengeStageEntity toEntity() {
        // challenge 참조를 null로 저장하면 같은 트랜잭션에서 1차 캐시로 재조회될 때
        // ChallengeStage.from()이 NPE로 죽는다. 모델이 들고 있는 값을 그대로 복원한다.
        return new ChallengeStageEntity(
            this.stageId,
            this.stageNumber,
            this.conditionAsCount,
            this.challenge != null ? this.challenge.toEntity() : null
        );
    }
}
