package io.openur.domain.NFT.repository;

import io.openur.domain.NFT.entity.NftAvatarWearingEntity;
import io.openur.domain.NFT.entity.NftAvatarWearingEntityId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftAvatarWearingJpaRepository extends JpaRepository<NftAvatarWearingEntity, NftAvatarWearingEntityId> {

    List<NftAvatarWearingEntity> findByUserId(String userId);

    void deleteByUserId(String userId);
}
