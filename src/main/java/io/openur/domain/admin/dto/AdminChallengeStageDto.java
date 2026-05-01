package io.openur.domain.admin.dto;

import io.openur.domain.challenge.entity.ChallengeStageEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminChallengeStageDto {

    private Long stageId;
    private Integer stageNumber;
    private Integer conditionCount;
    private long assignedUserChallengeCount;
    private boolean removable;

    public static AdminChallengeStageDto from(
        ChallengeStageEntity stage,
        long assignedUserChallengeCount
    ) {
        return AdminChallengeStageDto.builder()
            .stageId(stage.getStageId())
            .stageNumber(stage.getStageNumber())
            .conditionCount(stage.getConditionAsCount())
            .assignedUserChallengeCount(assignedUserChallengeCount)
            .removable(assignedUserChallengeCount == 0)
            .build();
    }
}
