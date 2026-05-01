package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChallengeJpaRepository extends JpaRepository<UserChallengeEntity, Long> {

    @EntityGraph(attributePaths = {
        "userEntity",
        "challengeStageEntity",
        "challengeStageEntity.challengeEntity"
    })
    Optional<UserChallengeEntity> findByUserChallengeId(Long userChallengeId);

    long countByChallengeStageEntityStageId(Long stageId);

    long countByChallengeStageEntityChallengeEntityChallengeId(Long challengeId);
} 
