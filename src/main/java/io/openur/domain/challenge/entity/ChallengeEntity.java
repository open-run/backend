package io.openur.domain.challenge.entity;

import io.openur.domain.challenge.model.ChallengeType;
import io.openur.domain.challenge.model.CompletedType;
import io.openur.domain.challenge.model.RewardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tb_challenges")
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;

    private String challengeName;

    @Enumerated(EnumType.STRING)
    private ChallengeType challengeType = ChallengeType.normal;

    private String description;

    @Enumerated(EnumType.STRING)
    private RewardType rewardType;

    @Column(precision = 5, scale = 2)
    private BigDecimal rewardPercentage;

    @Enumerated(EnumType.STRING)
    private CompletedType completedType;

    private Long completedConditionCount;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime completedConditionDate;

    private String completedConditionPlace;

    private Boolean completedConditionWearing;
}
