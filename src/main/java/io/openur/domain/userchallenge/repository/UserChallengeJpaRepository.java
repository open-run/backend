package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChallengeJpaRepository extends JpaRepository<UserChallengeEntity, Long> {

    List<UserChallengeEntity> findAllByUserEntity_UserId(String userId);
    
    @EntityGraph(attributePaths = {"challengeEntity"})
    Page<UserChallengeEntity> findAllByUserEntity_UserId(String userId, Pageable pageable);

    List<UserChallengeEntity> findAllByUserEntity_UserIdInAndChallengeStageEntity_ChallengeEntity_ChallengeId(List<String> userIds,
        List<Long> challengeIds);

    Optional<UserChallengeEntity> findByUserEntity_UserIdAndChallengeEntity_ChallengeId(String userId,
        Long challengeId);

    boolean existsByUserEntity_UserIdAndChallengeEntity_ChallengeId(String userId, Long challengeId);
} 
