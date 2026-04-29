package io.openur.domain.NFT.repository;

import io.openur.domain.NFT.entity.NftItemEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftItemJpaRepository extends JpaRepository<NftItemEntity, Long> {

    List<NftItemEntity> findByEnabledTrueAndNftTokenIdIsNotNullOrderByNftItemIdAsc();

    List<NftItemEntity> findByEnabledTrueOrderByNftItemIdAsc();

    List<NftItemEntity> findByNftItemIdIn(Collection<Long> nftItemIds);
}
