package io.openur.domain.NFT.repository;

import io.openur.domain.NFT.entity.NftItemEquipImageEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftItemEquipImageJpaRepository extends JpaRepository<NftItemEquipImageEntity, Long> {

    List<NftItemEquipImageEntity> findByNftItemEntity_NftItemIdInOrderByNftItemEntity_NftItemIdAscSortOrderAscNftItemEquipImageIdAsc(
        Collection<Long> nftItemIds
    );
}
