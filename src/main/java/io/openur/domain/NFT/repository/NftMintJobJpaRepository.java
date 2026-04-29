package io.openur.domain.NFT.repository;

import io.openur.domain.NFT.entity.NftMintJobEntity;
import java.util.List;
import java.util.Optional;
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

    @EntityGraph(attributePaths = {
        "userEntity",
        "userChallengeEntity",
        "userChallengeEntity.challengeStageEntity",
        "userChallengeEntity.challengeStageEntity.challengeEntity"
    })
    List<NftMintJobEntity> findTop20ByUserEntityUserIdOrderByUpdatedAtDesc(String userId);
}
