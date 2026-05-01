package io.openur.domain.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminChallengeStageRequestDto {

    private Long stageId;

    @NotNull(message = "stageNumber is required")
    @Positive(message = "stageNumber must be positive")
    private Integer stageNumber;

    @NotNull(message = "conditionCount is required")
    @Positive(message = "conditionCount must be positive")
    private Integer conditionCount;
}
