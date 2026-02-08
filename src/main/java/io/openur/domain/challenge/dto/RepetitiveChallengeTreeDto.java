package io.openur.domain.challenge.dto;

import io.openur.domain.challenge.model.Challenge;
import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.util.ArrayList;
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
        Challenge challenge = challengeStages.get(0).getChallenge();
        int previousCompletedCount = 0;
        UserChallenge currentChallenge = null;

        for (ChallengeStage stage : challengeStages) {
            UserChallenge uc = userChallengeMap.get(stage.getStageId());
            
            if(uc == null) continue;
            
            if(uc.getCompletedDate() == null) {
                currentChallenge = uc;
                break;
            }
            
            previousCompletedCount = uc.getCurrentCount();
        }

        final int prevCount = previousCompletedCount;
        final int currCount = currentChallenge != null ? currentChallenge.getCurrentCount() : 0;

        UserChallenge finalCurrentChallenge = currentChallenge;
        this.challengeTrees = challengeStages.stream().map(stage -> {
            UserChallenge uc = userChallengeMap.get(stage.getStageId());
            int accumulated = (uc == finalCurrentChallenge) ? prevCount : prevCount + currCount;
            return new RepetitiveChallengeDto(stage, uc, accumulated);
        }).toList();

        this.challengeId = challenge.getChallengeId();
        this.challengeName = challenge.getChallengeName();
        this.challengeDescription = challenge.getChallengeDescription();
    }
}
