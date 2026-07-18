package io.openur.domain.admin.dto;

import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminChallengeCompletionDto {

    private Long userChallengeId;
    private String userId;
    private String nickname;
    private Integer stageNumber;
    private Integer conditionCount;
    private LocalDateTime completedDate;
    private boolean nftCompleted;

    public static AdminChallengeCompletionDto from(UserChallengeEntity entity) {
        return AdminChallengeCompletionDto.builder()
            .userChallengeId(entity.getUserChallengeId())
            .userId(entity.getUserEntity().getUserId())
            .nickname(entity.getUserEntity().getNickname())
            .stageNumber(entity.getChallengeStageEntity().getStageNumber())
            .conditionCount(entity.getChallengeStageEntity().getConditionAsCount())
            .completedDate(entity.getCompletedDate())
            .nftCompleted(Boolean.TRUE.equals(entity.getNftCompleted()))
            .build();
    }
}
