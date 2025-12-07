package io.openur.domain.challenge.dto;

import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class RepetitiveChallengeDto {
    private Long userChallengeId = null;
    private LocalDateTime completedDate = null;
    private boolean nftCompleted = false;
    private Integer currentCount = 0;
    private Float currentProgress = 0.0f;

    private Long stageId;
    private Integer stageNumber;
    private Integer conditionAsCount;

    public RepetitiveChallengeDto(ChallengeStage stage, UserChallenge userChallenge) {
        this.stageId = stage.getStageId();
        this.stageNumber = stage.getStageNumber();
        this.conditionAsCount = stage.getConditionAsCount();

        if(userChallenge != null) {
            this.userChallengeId = userChallenge.getUserChallengeId();
            this.completedDate = userChallenge.getCompletedDate();
            this.nftCompleted = userChallenge.getNftCompleted();
            this.currentCount = userChallenge.getCurrentCount();
            this.currentProgress = userChallenge.getCurrentProgress();
        }
    }
}
