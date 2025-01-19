package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.model.UserChallenge;
import java.util.List;
import java.util.Optional;

public interface UserChallengeRepository {

    List<UserChallenge> saveAll(List<UserChallenge> userChallenges);

    List<UserChallenge> findByUserId(String userId);

    Optional<UserChallenge> findOptionalByUserIdAndChallengeId(String userId, Long challengeId);

    boolean existsByUserIdAndChallengeId(String userId, Long challengeId);

    void delete(UserChallenge userChallenge);
} 