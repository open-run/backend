package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.entity.ChallengeEntity;
import io.openur.domain.challenge.model.Challenge;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChallengeRepositoryImpl implements ChallengeRepository {

    private final ChallengeJpaRepository challengeJpaRepository;

    @Override
    public Challenge save(Challenge challenge) {
        return Challenge.from(challengeJpaRepository.save(challenge.toEntity()));
    }

    @Override
    public Challenge findById(Long challengeId) {
        ChallengeEntity challengeEntity = challengeJpaRepository.findById(challengeId)
            .orElseThrow(() -> new NoSuchElementException("Challenge not found"));
        return Challenge.from(challengeEntity);
    }

    @Override
    public List<Challenge> findAll() {
        return challengeJpaRepository.findAll().stream()
            .map(Challenge::from)
            .toList();
    }

    @Override
    public void delete(Challenge challenge) {
        challengeJpaRepository.delete(challenge.toEntity());
    }
} 
