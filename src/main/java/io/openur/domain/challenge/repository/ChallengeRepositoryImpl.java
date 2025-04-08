package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.model.Challenge;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChallengeRepositoryImpl implements ChallengeRepository {
    
    private final ChallengeJpaRepository challengeJpaRepository;

    @Override
    public Challenge findById(Long challengeId) {
        return null;
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
