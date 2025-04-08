package io.openur.domain.userchallenge.dto;

import io.openur.domain.challenge.model.ChallengeType;
import io.openur.domain.challenge.model.CompletedType;
import io.openur.domain.challenge.model.RewardType;
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
    private Integer conditionAsCount;
    private LocalDateTime conditionAsDate;
    private String conditionAsText;
    
    public UserChallengeInfoDto(UserChallenge userChallenge) {
        this.userChallengeId = userChallenge.getUserChallengeId();
        this.currentCount = userChallenge.getCurrentCount();
        this.completedDate = userChallenge.getCompletedDate();
        this.accomplished = userChallenge.getCompletedDate() != null;
        this.nftCompleted = userChallenge.getNftCompleted();
        this.challengeId = userChallenge.getChallenge().getChallengeId();
        this.challengeName = userChallenge.getChallenge().getChallengeName();
        this.challengeDescription = userChallenge.getChallenge().getChallengeDescription();
        this.challengeType = userChallenge.getChallenge().getChallengeType();
        this.rewardType = userChallenge.getChallenge().getRewardType();
        this.completedType = userChallenge.getChallenge().getCompletedType();
        this.conditionAsCount = userChallenge.getChallenge().getCompletedConditionCount();
        this.conditionAsDate = userChallenge.getChallenge().getCompletedConditionDate();
        this.conditionAsText = userChallenge.getChallenge().getCompletedConditionText();
    }
}
