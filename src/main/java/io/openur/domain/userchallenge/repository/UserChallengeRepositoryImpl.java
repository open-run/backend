package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserChallengeRepositoryImpl implements UserChallengeRepository {

    private final UserChallengeJpaRepository userChallengeJpaRepository;

    @Override
    public UserChallenge save(UserChallenge userChallenge) {
        return UserChallenge.from(userChallengeJpaRepository.save(userChallenge.toEntity()));
    }

    @Override
    public List<UserChallenge> findByUserId(String userId) {
        return userChallengeJpaRepository.findByUserEntity_UserId(userId).stream()
            .map(UserChallenge::from)
            .toList();
    }

    @Override
    public UserChallenge findByUserIdAndChallengeId(String userId, Long challengeId) {
        UserChallengeEntity entity = userChallengeJpaRepository
            .findByUserEntity_UserIdAndChallengeEntity_ChallengeId(userId, challengeId)
            .orElseThrow(() -> new NoSuchElementException("UserChallenge not found"));
        return UserChallenge.from(entity);
    }

    @Override
    public boolean existsByUserIdAndChallengeId(String userId, Long challengeId) {
        return userChallengeJpaRepository.existsByUserEntity_UserIdAndChallengeEntity_ChallengeId(userId, challengeId);
    }

    @Override
    public void delete(UserChallenge userChallenge) {
        userChallengeJpaRepository.delete(userChallenge.toEntity());
    }
} 