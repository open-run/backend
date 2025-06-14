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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Getter
@Table(name = "tb_challenges")
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;
    
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "challenge_type")
    private ChallengeType challengeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type")
    private RewardType rewardType;
    
    @Column(name = "reward_percentage")
    private Float rewardPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "completed_type")
    private CompletedType completedType;
    
    // 단순 횟수
    @Column(name = "condition_count")
    private Integer conditionAsCount;
    
    // 날짜 조건
    @Column(name = "condition_date")
    private LocalDateTime conditionAsDate;
    
    // 페이스, 장소 등
    @Column(name = "condition_text")
    private String conditionAsText;
    
}
