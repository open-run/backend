package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChallengeJpaRepository extends JpaRepository<UserChallengeEntity, Long> {

    List<UserChallengeEntity> findAllByUserEntity_UserId(String userId);
    
    @EntityGraph(attributePaths = {"challengeEntity"})
    Page<UserChallengeEntity> findAllByUserEntity_UserId(String userId, Pageable pageable);

    List<UserChallengeEntity> findAllByUserEntity_UserIdInAndChallengeEntity_ChallengeIdIn(List<String> userIds,
        List<Long> challengeIds);

    Optional<UserChallengeEntity> findByUserEntity_UserIdAndChallengeEntity_ChallengeId(String userId,
        Long challengeId);

    boolean existsByUserEntity_UserIdAndChallengeEntity_ChallengeId(String userId, Long challengeId);
} 
