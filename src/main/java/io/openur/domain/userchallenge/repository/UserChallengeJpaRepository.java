package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserChallengeJpaRepository extends JpaRepository<UserChallengeEntity, Long> {

    @Query("""
        select uc from UserChallengeEntity uc
        join fetch uc.userEntity
        join fetch uc.challengeStageEntity cs
        where cs.challengeEntity.challengeId = :challengeId
          and uc.completedDate is not null
        order by uc.completedDate desc, uc.userChallengeId desc
        """)
    List<UserChallengeEntity> findCompletionsByChallengeId(@Param("challengeId") Long challengeId);

    @EntityGraph(attributePaths = {
        "userEntity",
        "challengeStageEntity",
        "challengeStageEntity.challengeEntity"
    })
    Optional<UserChallengeEntity> findByUserChallengeId(Long userChallengeId);

    long countByChallengeStageEntityStageId(Long stageId);

    long countByChallengeStageEntityChallengeEntityChallengeId(Long challengeId);
} 
