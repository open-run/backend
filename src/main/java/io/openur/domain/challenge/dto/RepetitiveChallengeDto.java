package io.openur.domain.challenge.dto;

import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RepetitiveChallengeDto {
    private Long userChallengeId = null;
    private LocalDateTime completedDate = null;
    private boolean nftCompleted = false;
    private Integer currentCount = 0;
    private Float currentProgress = 0.0f;

    private Long stageId;
    private Integer stageNumber;
    private Integer conditionAsCount;

    public RepetitiveChallengeDto(
        ChallengeStage stage, UserChallenge userChallenge, int accumulatedCount
    ) {
        this.stageId = stage.getStageId();
        this.stageNumber = stage.getStageNumber();
        this.conditionAsCount = stage.getConditionAsCount();
        this.currentCount = accumulatedCount;
        this.currentProgress = (float) this.currentCount / this.conditionAsCount;
        // 조건 변경으로 완수 혹은 nft 발급된 것도 나와야 할듯 하다.
        // 
        if(userChallenge != null) {
            this.userChallengeId = userChallenge.getUserChallengeId();
            this.completedDate = userChallenge.getCompletedDate();
            this.nftCompleted = userChallenge.getNftCompleted();
        }
    }
}
