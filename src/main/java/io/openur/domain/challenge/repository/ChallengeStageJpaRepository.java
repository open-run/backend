package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.entity.ChallengeStageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeStageJpaRepository extends
    JpaRepository<ChallengeStageEntity, Long>
{
    List<ChallengeStageEntity> findAllByChallengeEntityChallengeIdOrderByStageNumberAsc(Long challengeId);
}
