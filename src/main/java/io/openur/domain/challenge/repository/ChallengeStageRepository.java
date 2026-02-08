package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.model.ChallengeStage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChallengeStageRepository {

    List<ChallengeStage> findAllByChallengeId(Long challengeId);

    Optional<ChallengeStage> findByChallengeIdAndStageIsGreaterThan(Long challengeId, Integer stage);

    Page<ChallengeStage> findAllByMinimumStages(Pageable pageable);
}
