package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.model.UserChallenge;
import java.util.List;

public interface UserChallengeRepository {

    UserChallenge save(UserChallenge userChallenge);

    List<UserChallenge> findByUserId(String userId);

    UserChallenge findByUserIdAndChallengeId(String userId, Long challengeId);

    boolean existsByUserIdAndChallengeId(String userId, Long challengeId);

    void delete(UserChallenge userChallenge);
} 