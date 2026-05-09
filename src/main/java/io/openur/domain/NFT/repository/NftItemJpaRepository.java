package io.openur.domain.NFT.repository;

import io.openur.domain.NFT.entity.NftItemEntity;
import io.openur.domain.NFT.enums.NftItemCategory;
import io.openur.domain.NFT.enums.NftItemRarity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftItemJpaRepository extends JpaRepository<NftItemEntity, Long> {

    List<NftItemEntity> findByEnabledTrueAndNftTokenIdIsNotNullOrderByNftItemIdAsc();

    List<NftItemEntity> findByEnabledTrueOrderByNftItemIdAsc();

    List<NftItemEntity> findByNftItemIdIn(Collection<Long> nftItemIds);

    Optional<NftItemEntity> findByNftTokenId(String nftTokenId);

    long countByCategoryAndRarityAndEnabledTrueAndNftTokenIdIsNotNull(
        NftItemCategory category, NftItemRarity rarity);

    Page<NftItemEntity> findByCategoryAndRarityAndEnabledTrueAndNftTokenIdIsNotNullOrderByNftItemIdAsc(
        NftItemCategory category, NftItemRarity rarity, Pageable pageable);
}
