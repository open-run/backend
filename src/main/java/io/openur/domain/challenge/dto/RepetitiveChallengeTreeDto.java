package io.openur.domain.challenge.dto;

import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RepetitiveChallengeTreeDto {
    private Long challengeId;
    private String challengeName;
    private String challengeDescription;
    private List<RepetitiveChallengeDto> challengeTrees;

    public RepetitiveChallengeTreeDto(
        Map<Long, UserChallenge> userChallengeMap, List<ChallengeStage> challengeStages
    ) {
        ChallengeStage firstStage = challengeStages.get(0);
        int accumulatedCount = 0;
        for (ChallengeStage challengeStage : challengeStages) {
            if(userChallengeMap.get(challengeStage.getStageId()).getCompletedDate() == null)
                break;
            accumulatedCount = challengeStage.getConditionAsCount();
        }

        final int finalAccumulatedCount = accumulatedCount;
        this.challengeTrees = challengeStages.stream().map(stage ->
            new RepetitiveChallengeDto(
                stage, userChallengeMap.get(stage.getStageId()),
                finalAccumulatedCount
            )
        ).toList();

        // 공통 옵션
        this.challengeId = firstStage.getChallenge().getChallengeId();
        this.challengeName = firstStage.getChallenge().getChallengeName();
        this.challengeDescription = firstStage.getChallenge().getChallengeDescription();
    }
}
