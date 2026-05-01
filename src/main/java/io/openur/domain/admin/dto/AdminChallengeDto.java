package io.openur.domain.admin.dto;

import io.openur.domain.challenge.entity.ChallengeEntity;
import io.openur.domain.challenge.entity.ChallengeStageEntity;
import io.openur.domain.challenge.enums.ChallengeType;
import io.openur.domain.challenge.enums.CompletedType;
import io.openur.domain.challenge.enums.RewardType;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminChallengeDto {

    private Long challengeId;
    private String name;
    private String description;
    private ChallengeType challengeType;
    private RewardType rewardType;
    private CompletedType completedType;
    private LocalDateTime conditionDate;
    private String conditionText;
    private long assignedUserChallengeCount;
    private boolean deletable;
    private List<AdminChallengeStageDto> stages;

    public static AdminChallengeDto from(
        ChallengeEntity challenge,
        long assignedUserChallengeCount,
        Map<Long, Long> stageAssignmentCounts
    ) {
        List<ChallengeStageEntity> challengeStages = challenge.getChallengeStages() == null
            ? List.of()
            : challenge.getChallengeStages();
        List<AdminChallengeStageDto> stages = challengeStages.stream()
            .sorted(Comparator.comparing(ChallengeStageEntity::getStageNumber)
                .thenComparing(ChallengeStageEntity::getStageId))
            .map(stage -> AdminChallengeStageDto.from(
                stage,
                stageAssignmentCounts.getOrDefault(stage.getStageId(), 0L)
            ))
            .toList();

        return AdminChallengeDto.builder()
            .challengeId(challenge.getChallengeId())
            .name(challenge.getName())
            .description(challenge.getDescription())
            .challengeType(challenge.getChallengeType())
            .rewardType(challenge.getRewardType())
            .completedType(challenge.getCompletedType())
            .conditionDate(challenge.getConditionAsDate())
            .conditionText(challenge.getConditionAsText())
            .assignedUserChallengeCount(assignedUserChallengeCount)
            .deletable(assignedUserChallengeCount == 0)
            .stages(stages)
            .build();
    }
}
