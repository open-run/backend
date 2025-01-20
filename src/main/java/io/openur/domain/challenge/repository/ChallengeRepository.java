package io.openur.domain.challenge.repository;
import io.openur.domain.challenge.model.Challenge;
import java.util.List;
public interface ChallengeRepository {
    Challenge save(Challenge challenge);
    Boolean existsById(Long challengeId);
    Challenge findById(Long challengeId);
    List<Challenge> findAll();
    void delete(Challenge challenge);
}
