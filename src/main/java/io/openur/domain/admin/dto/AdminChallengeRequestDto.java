package io.openur.domain.admin.dto;

import io.openur.domain.challenge.enums.ChallengeType;
import io.openur.domain.challenge.enums.CompletedType;
import io.openur.domain.challenge.enums.RewardType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminChallengeRequestDto {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "challengeType is required")
    private ChallengeType challengeType;

    @NotNull(message = "rewardType is required")
    private RewardType rewardType;

    @NotNull(message = "completedType is required")
    private CompletedType completedType;

    private LocalDateTime conditionDate;

    private String conditionText;

    @Valid
    @NotEmpty(message = "stages is required")
    private List<AdminChallengeStageRequestDto> stages;
}
