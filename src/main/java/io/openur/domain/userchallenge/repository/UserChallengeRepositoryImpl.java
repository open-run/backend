package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.model.UserChallenge;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserChallengeRepositoryImpl implements UserChallengeRepository {

    private final UserChallengeJpaRepository userChallengeJpaRepository;

    @Override
    public UserChallenge save(UserChallenge userChallenge) {
        return UserChallenge.from(
            userChallengeJpaRepository.save(userChallenge.toEntity()));
    }

    @Override
    public List<UserChallenge> findByUserId(String userId) {
        return userChallengeJpaRepository.findByUserEntity_UserId(userId)
            .stream()
            .map(UserChallenge::from)
            .toList();
    }

    @Override
    public Optional<UserChallenge> findOptionalByUserIdAndChallengeId(String userId, Long challengeId){
            return userChallengeJpaRepository
                .findByUserEntity_UserIdAndChallengeEntity_ChallengeId(userId,
                    challengeId).map(UserChallenge::from);
        }

    @Override
    public boolean existsByUserIdAndChallengeId (String userId, Long
    challengeId){
        return userChallengeJpaRepository.existsByUserEntity_UserIdAndChallengeEntity_ChallengeId(
            userId, challengeId);
    }

    @Override
    public void delete (UserChallenge userChallenge){
        userChallengeJpaRepository.delete(userChallenge.toEntity());
    }
}
