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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NftTokenJpaRepository extends JpaRepository<NftTokenEntity, String> {

    @EntityGraph(attributePaths = {"nft"})
    @Query("select t from NftTokenEntity t where t.imageRole = :imageRole order by t.nft.nftId asc")
    List<NftTokenEntity> findByImageRole(@Param("imageRole") NftImageRole imageRole);

    @EntityGraph(attributePaths = {"nft"})
    List<NftTokenEntity> findByTokenIdInAndImageRole(Collection<String> tokenIds, NftImageRole imageRole);

    @EntityGraph(attributePaths = {"nft"})
    Optional<NftTokenEntity> findByTokenIdAndImageRole(String tokenId, NftImageRole imageRole);

    @Query("select count(t) from NftTokenEntity t "
        + "where t.imageRole = :imageRole and t.nft.category = :category and t.nft.rarity = :rarity")
    long countCandidates(
        @Param("imageRole") NftImageRole imageRole,
        @Param("category") NftItemCategory category,
        @Param("rarity") NftItemRarity rarity);

    @EntityGraph(attributePaths = {"nft"})
    @Query("select t from NftTokenEntity t "
        + "where t.imageRole = :imageRole and t.nft.category = :category and t.nft.rarity = :rarity "
        + "order by t.nft.nftId asc")
    List<NftTokenEntity> findCandidates(
        @Param("imageRole") NftImageRole imageRole,
        @Param("category") NftItemCategory category,
        @Param("rarity") NftItemRarity rarity,
        Pageable pageable);
}
