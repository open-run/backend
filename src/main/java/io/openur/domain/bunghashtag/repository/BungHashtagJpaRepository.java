package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BungHashtagJpaRepository extends JpaRepository<BungHashtagEntity, Long> {

    List<BungHashtagEntity> findByBungEntity_BungId(String bungId);
    
    @EntityGraph(attributePaths = {"hashtagEntity"})
    List<BungHashtagEntity> findAllByBungEntity_BungIdAndHashtagEntity_HashtagStrIn(
        String bungId, List<String> hashtagStrList
    );

    void deleteByBungEntity_BungId(String bungId);

}
