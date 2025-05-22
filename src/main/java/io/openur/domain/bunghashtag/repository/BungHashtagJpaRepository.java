package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface BungHashtagJpaRepository extends JpaRepository<BungHashtagEntity, Long> {

    List<BungHashtagEntity> findByBungEntity_BungId(String bungId);
    
    List<BungHashtagEntity> findAllByBungEntity_BungId(String bungId);

    void deleteByBungEntity_BungId(String bungId);
    
    @Modifying
    @Transactional
    void deleteAllByBungEntity_BungIdAndHashtagEntity_HashtagStrNotIn(
        String bungEntityBungId,
        Set<String> hashtagEntityHashtagStr
    );
}
