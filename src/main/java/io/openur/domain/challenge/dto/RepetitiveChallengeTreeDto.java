package io.openur.domain.challenge.dto;

import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class RepetitiveChallengeTreeDto {
    private Long challengeId;
    private String challengeName;
    private String challengeDescription;
    private List<RepetitiveChallengeDto> challenges;

    public RepetitiveChallengeTreeDto(
        List<ChallengeStage> challengeStages, Map<Long, UserChallenge> userChallengeMap
    ) {
        this.challenges = challengeStages.stream().map(
            stage ->
                new RepetitiveChallengeDto(
                    // 없으면 알아서 null 로 나올것임
                    stage, userChallengeMap.get(stage.getStageId()
                ))
        ).toList();

        ChallengeStage firstStage = challengeStages.getFirst();
        // 공통 옵션
        this.challengeId = firstStage.getChallenge().getChallengeId();
        this.challengeName = firstStage.getChallenge().getChallengeName();
        this.challengeDescription = firstStage.getChallenge().getChallengeDescription();
    }
}
