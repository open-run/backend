package io.openur.domain.bung.repository;

import io.openur.domain.bung.entity.BungEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BungJpaRepository extends JpaRepository<BungEntity, String> {

    void deleteByBungId(String bungId);

    BungEntity findBungEntityByBungId(String bungId);
}
