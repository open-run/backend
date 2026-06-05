package io.openur.domain.NFT.repository;

import io.openur.domain.NFT.entity.NftTokenEntity;
import io.openur.domain.NFT.enums.NftImageRole;
import io.openur.domain.NFT.enums.NftItemCategory;
import io.openur.domain.NFT.enums.NftItemRarity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftTokenJpaRepository extends JpaRepository<NftTokenEntity, String> {

    @EntityGraph(attributePaths = {"nft"})
    List<NftTokenEntity> findByImageRoleOrderByNftNftIdAsc(NftImageRole imageRole);

    @EntityGraph(attributePaths = {"nft"})
    List<NftTokenEntity> findByTokenIdInAndImageRole(Collection<String> tokenIds, NftImageRole imageRole);

    @EntityGraph(attributePaths = {"nft"})
    Optional<NftTokenEntity> findByTokenIdAndImageRole(String tokenId, NftImageRole imageRole);

    long countByImageRoleAndNftCategoryAndNftRarity(
        NftImageRole imageRole, NftItemCategory category, NftItemRarity rarity);

    @EntityGraph(attributePaths = {"nft"})
    List<NftTokenEntity> findByImageRoleAndNftCategoryAndNftRarityOrderByNftNftIdAsc(
        NftImageRole imageRole, NftItemCategory category, NftItemRarity rarity, Pageable pageable);
}
