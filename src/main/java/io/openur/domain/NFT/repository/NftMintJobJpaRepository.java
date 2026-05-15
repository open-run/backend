package io.openur.domain.NFT.repository;

import io.openur.domain.NFT.entity.NftMintJobEntity;
import io.openur.domain.NFT.enums.NftMintJobStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftMintJobJpaRepository extends JpaRepository<NftMintJobEntity, Long> {

    @EntityGraph(attributePaths = {
        "userEntity",
        "userChallengeEntity",
        "userChallengeEntity.challengeStageEntity",
        "userChallengeEntity.challengeStageEntity.challengeEntity"
    })
    Optional<NftMintJobEntity> findByUserChallengeEntityUserChallengeId(Long userChallengeId);

    List<NftMintJobEntity> findByUserChallengeEntityUserChallengeIdInAndStatus(
        Collection<Long> userChallengeIds, NftMintJobStatus status);

    Slice<NftMintJobEntity> findByStatusAndUpdatedAtBefore(
        NftMintJobStatus status, LocalDateTime cutoff, Pageable pageable);
}
