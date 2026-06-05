package io.openur.domain.NFT.repository;

import io.openur.domain.NFT.entity.NftEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftJpaRepository extends JpaRepository<NftEntity, Integer> {

    List<NftEntity> findAllByOrderByNftIdAsc();
}
