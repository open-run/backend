package io.openur.domain.userchallenge.dto;

import io.openur.domain.challenge.entity.ChallengeEntity;
import io.openur.domain.challenge.entity.ChallengeStageEntity;
import io.openur.domain.challenge.enums.ChallengeType;
import io.openur.domain.challenge.enums.CompletedType;
import io.openur.domain.challenge.enums.RewardType;
import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class UserChallengeInfoDto {
    private Long userChallengeId;
    private Integer currentCount;
    private LocalDateTime completedDate;
    private Boolean accomplished;
    private Boolean nftCompleted;
    private Long challengeId;
    private String challengeName;
    private String challengeDescription;
    private ChallengeType challengeType;
    private RewardType rewardType;
    private CompletedType completedType;
    private Integer stageCount;
    private Integer conditionAsCount;
    private LocalDateTime conditionAsDate;
    private String conditionAsText;
    
    public UserChallengeInfoDto(UserChallengeEntity userChallenge) {
        this.userChallengeId = userChallenge.getUserChallengeId();
        this.currentCount = userChallenge.getCurrentCount();
        this.completedDate = userChallenge.getCompletedDate();
        this.accomplished = userChallenge.getCompletedDate() != null;
        this.nftCompleted = userChallenge.getNftCompleted();

        ChallengeStageEntity challengeStageEntity = userChallenge.getChallengeStageEntity();
        this.conditionAsCount = challengeStageEntity.getConditionAsCount();
        this.stageCount = challengeStageEntity.getStageNumber();

        ChallengeEntity challengeEntity = challengeStageEntity.getChallengeEntity();
        this.challengeId = challengeEntity.getChallengeId();
        this.challengeName = challengeEntity.getName();
        this.challengeDescription = challengeEntity.getDescription();
        this.challengeType = challengeEntity.getChallengeType();
        this.rewardType = challengeEntity.getRewardType();
        this.completedType = challengeEntity.getCompletedType();
        this.conditionAsDate = challengeEntity.getConditionAsDate();
        this.conditionAsText = challengeEntity.getConditionAsText();
    }
}
