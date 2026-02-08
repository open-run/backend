package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.model.ChallengeStage;
import java.util.List;
import java.util.Optional;

public interface ChallengeStageRepository {

    List<ChallengeStage> findAllByChallengeId(Long challengeId);

    Optional<ChallengeStage> findByChallengeIdAndStageIsGreaterThan(Long challengeId, Integer stage);
}
