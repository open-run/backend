package io.openur.domain.challenge.dto;

import io.openur.domain.challenge.model.Challenge;
import io.openur.domain.challenge.model.ChallengeType;
import io.openur.domain.challenge.model.CompletedType;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ChallengeInfoDto {
    private Long challengeId;
    private Long userChallengeId;
    private String challengeName;
    private String challengeDescription;
    private Integer currentCount;
    private Integer conditionCount;
    private LocalDateTime conditionDate;
    private String conditionText;
    private ChallengeType challengeType;
    private CompletedType completedType;
    private boolean completed;
    private boolean nftCompleted;

    public ChallengeInfoDto(UserChallenge userChallenge) {
        Challenge challenge = userChallenge.getChallenge();
        this.challengeId = challenge.getChallengeId();
        this.userChallengeId = userChallenge.getUserChallengeId();
        this.challengeName = challenge.getChallengeName();
        this.challengeDescription = challenge.getChallengeDescription();
        this.currentCount = userChallenge.getCurrentCount();
        this.conditionCount = challenge.getCompletedConditionCount();
        this.conditionDate = challenge.getCompletedConditionDate();
        this.conditionText = challenge.getCompletedConditionText();
        this.challengeType = challenge.getChallengeType();
        this.completedType = challenge.getCompletedType();
        this.completed = userChallenge.getCompletedDate() != null;
        this.nftCompleted = userChallenge.getNftCompleted();
    }
}
