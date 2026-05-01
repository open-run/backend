package io.openur.domain.challenge.repository;

import io.openur.domain.challenge.entity.ChallengeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeJpaRepository extends JpaRepository<ChallengeEntity, Long> {

    @Query("select distinct c from ChallengeEntity c left join fetch c.challengeStages order by c.challengeId asc")
    List<ChallengeEntity> findAllByOrderByChallengeIdAsc();

    @Query("select distinct c from ChallengeEntity c left join fetch c.challengeStages where c.challengeId = :challengeId")
    Optional<ChallengeEntity> findByChallengeId(@Param("challengeId") Long challengeId);
}
