package io.openur.domain.bung.repository;

import io.openur.domain.bung.entity.BungEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BungJpaRepository extends JpaRepository<BungEntity, String> {

    void deleteByBungId(String bungId);
    
    @EntityGraph(attributePaths = {"bungHashtags", "bungHashtags.hashtagEntity"})
    BungEntity findBungEntityByBungId(String bungId);
}
