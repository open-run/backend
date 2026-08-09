package io.openur.domain.challenge.dto;

import io.openur.domain.challenge.enums.ChallengeType;
import io.openur.domain.challenge.enums.CompletedType;
import io.openur.domain.challenge.model.Challenge;
import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GeneralChallengeDto {
    private Long challengeId;
    private Long userChallengeId;
    private String challengeName;
    private LocalDateTime completedDate;
    private String challengeDescription;
    private Integer currentCount;
    private Integer conditionCount;
    private LocalDateTime conditionDate;
    private String conditionText;
    private ChallengeType challengeType;
    private CompletedType completedType;
    private Integer stageCount;
    private float progressStat = 0.0f;
    private boolean accomplished;
    private boolean nftCompleted;

    public GeneralChallengeDto(UserChallenge userChallenge) {
        this(userChallenge.getChallengeStage(), userChallenge);
    }

    public GeneralChallengeDto(ChallengeStage challengeStage, UserChallenge userChallenge) {
        if (userChallenge != null) {
            this.userChallengeId = userChallenge.getUserChallengeId();
            this.currentCount = userChallenge.getCurrentCount();
            this.completedDate = userChallenge.getCompletedDate();
            this.accomplished = userChallenge.getCompletedDate() != null;
            this.nftCompleted = userChallenge.getNftCompleted();
        } else {
            this.currentCount = 0;
        }

        this.conditionCount = challengeStage.getConditionAsCount();
        this.stageCount = challengeStage.getStageNumber();
        // 진행률은 응답 시점에 계산한다. 저장된 current_progress 는 카운트 증가 경로에서
        // 갱신되지 않아 stale 이므로 읽지 않는다.
        this.progressStat = UserChallenge.toProgressPercent(this.currentCount, this.conditionCount);

        Challenge challenge = challengeStage.getChallenge();
        this.challengeId = challenge.getChallengeId();
        this.challengeName = challenge.getChallengeName();
        this.challengeDescription = challenge.getChallengeDescription();
        this.completedType = challenge.getCompletedType();
        this.conditionDate = challenge.getCompletedConditionDate();
        this.conditionText = challenge.getCompletedConditionText();
        this.challengeType = challenge.getChallengeType();
    }
}
