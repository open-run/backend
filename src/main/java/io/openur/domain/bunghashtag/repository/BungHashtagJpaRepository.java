package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BungHashtagJpaRepository extends JpaRepository<BungHashtagEntity, Long> {

    List<BungHashtagEntity> findByBungEntity_BungId(String bungId);
    
    List<BungHashtagEntity> findAllByBungEntity_BungId(String bungId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BungHashtagEntity bhe WHERE bhe.bungEntity.bungId = :bungId")
    void deleteByBungEntity_BungId(@Param("bungId") String bungId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM BungHashtagEntity bhe WHERE bhe.bungEntity.bungId = :bungId AND bhe.hashtagEntity.hashtagStr NOT IN :hashtagStrs")
    void deleteAllByBungEntity_BungIdAndHashtagEntity_HashtagStrNotIn(
        @Param("bungId") String bungEntityBungId,
        @Param("hashtagStrs") Set<String> hashtagEntityHashtagStr
    );
}
