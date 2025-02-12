package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChallengeJpaRepository extends JpaRepository<UserChallengeEntity, Long> {

    List<UserChallengeEntity> findByUserEntity_UserId(String userId);

    Optional<UserChallengeEntity> findByUserEntity_UserIdInAndChallengeIdIn(List<String> userIds,
        List<Long> challengeIds);

    Optional<UserChallengeEntity> findByUserEntity_UserIdAndChallengeId(String userId,
        Long challengeId);

    boolean existsByUserEntity_UserIdAndChallengeId(String userId, Long challengeId);
} 
