package io.openur.domain.challenge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Getter
@Table(name = "tb_challenge_stages")
@NoArgsConstructor
public class ChallengeStageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stageId;

    private Integer stageNumber;

    @Column(name = "condition_count")
    private Integer conditionAsCount;

    @ManyToOne(targetEntity = ChallengeEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private ChallengeEntity challengeEntity;

    @Column(name = "weight_common", nullable = false)
    private Integer weightCommon = 70;

    @Column(name = "weight_rare", nullable = false)
    private Integer weightRare = 25;

    @Column(name = "weight_epic", nullable = false)
    private Integer weightEpic = 5;

    public ChallengeStageEntity(
        Long stageId,
        Integer stageNumber,
        Integer conditionAsCount,
        ChallengeEntity challengeEntity
    ) {
        this.stageId = stageId;
        this.stageNumber = stageNumber;
        this.conditionAsCount = conditionAsCount;
        this.challengeEntity = challengeEntity;
    }

    public void update(Integer stageNumber, Integer conditionAsCount) {
        this.stageNumber = stageNumber;
        this.conditionAsCount = conditionAsCount;
    }

    public void assignChallenge(ChallengeEntity challengeEntity) {
        this.challengeEntity = challengeEntity;
    }
}
