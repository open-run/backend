package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.entity.ChallengeStageEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeStageJpaRepository extends
    JpaRepository<ChallengeStageEntity, Long>
{
    List<ChallengeStageEntity> findAllByChallengeEntityChallengeIdOrderByStageNumberAsc(Long challengeId);

    Optional<ChallengeStageEntity> findFirstByChallengeEntity_ChallengeIdAndStageNumberIsGreaterThan(Long challengeId, Integer stage);
}
