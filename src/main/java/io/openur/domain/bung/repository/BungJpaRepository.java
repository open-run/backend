package io.openur.domain.bung.repository;

import io.openur.domain.bung.entity.BungEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BungJpaRepository extends JpaRepository<BungEntity, Long> {
    void deleteByBungId(String bungId);
}
