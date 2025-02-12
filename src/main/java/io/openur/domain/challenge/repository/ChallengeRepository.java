package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.model.Challenge;
import java.util.List;

public interface ChallengeRepository {

    Challenge findById(Long challengeId);

    List<Challenge> findAll();

    void delete(Challenge challenge);
}
