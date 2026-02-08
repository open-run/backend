package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.model.ChallengeStage;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeStageRepositoryImpl implements ChallengeStageRepository {
    private final ChallengeStageJpaRepository challengeStageJpaRepository;

    @Override
    public List<ChallengeStage> findAllByChallengeId(Long challengeId) {
        return challengeStageJpaRepository
            .findAllByChallengeEntityChallengeIdOrderByStageNumberAsc(challengeId)
            .stream()
            .map(ChallengeStage::from)
            .toList();
    }

    @Override
    public Optional<ChallengeStage> findByChallengeIdAndStageIsGreaterThan(
        Long challengeId, Integer stage) {
        return challengeStageJpaRepository
            .findFirstByChallengeEntity_ChallengeIdAndStageNumberIsGreaterThan(
                challengeId, stage
            )
            .map(ChallengeStage::from);
    }
}
