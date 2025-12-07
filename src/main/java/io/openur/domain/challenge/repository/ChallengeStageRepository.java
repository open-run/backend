package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.model.ChallengeStage;
import java.util.List;

public interface ChallengeStageRepository {

    List<ChallengeStage> findAllByChallengeId(Long challengeId);
}
