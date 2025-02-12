package io.openur.domain.challenge.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Challenge {

    private Long challengeId;
    private String challengeName;
    private ChallengeType challengeType;
    private String description;
    private RewardType rewardType;
    private BigDecimal rewardPercentage;
    private CompletedType completedType;
    private Long completedConditionCount;
    private LocalDateTime completedConditionDate;
    private String completedConditionPlace;
    private Boolean completedConditionWearing;
}
