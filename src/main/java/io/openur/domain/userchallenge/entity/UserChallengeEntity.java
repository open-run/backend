package io.openur.domain.userchallenge.entity;

import io.openur.domain.challenge.entity.ChallengeStageEntity;
import io.openur.domain.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tb_users_challenges")
@NoArgsConstructor
@AllArgsConstructor
public class UserChallengeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userChallengeId;

    @ManyToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;
    
    @OneToOne(targetEntity = ChallengeStageEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_stage_id", nullable = false)
    private ChallengeStageEntity challengeStageEntity;

    private Integer currentCount;

    private LocalDateTime completedDate;

    @Column(nullable = false)
    private Boolean nftCompleted;
}
