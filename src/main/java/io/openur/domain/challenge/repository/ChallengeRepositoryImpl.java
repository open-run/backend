package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.model.Challenge;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeRepositoryImpl implements ChallengeRepository {
    
    private final ChallengeJpaRepository challengeJpaRepository;

    @Override
    public Challenge findById(Long challengeId) {
        return Challenge.from(
            challengeJpaRepository.findById(challengeId)
                .orElseThrow( ()-> new RuntimeException("Challenge not found"))
        );
    }

    @Override
    public List<Challenge> findAll() {
        return null;
    }

    @Override
    public void delete(Challenge challenge) {
        return;
    }
}
