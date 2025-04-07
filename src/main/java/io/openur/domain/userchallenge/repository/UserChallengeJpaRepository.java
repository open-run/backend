package io.openur.domain.userchallenge.repository;

import io.openur.domain.user.entity.UserEntity;
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
    Page<UserChallengeEntity> findAllByUserEntity(UserEntity userEntity, Pageable pageable);

    List<UserChallengeEntity> findAllByUserEntity_UserIdInAndChallengeIdIn(List<String> userIds,
        List<Long> challengeIds);

    Optional<UserChallengeEntity> findByUserEntity_UserIdAndChallengeId(String userId,
        Long challengeId);

    boolean existsByUserEntity_UserIdAndChallengeId(String userId, Long challengeId);
} 
