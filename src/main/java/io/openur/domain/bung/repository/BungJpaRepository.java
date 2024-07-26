package io.openur.domain.bung.repository;

import io.openur.domain.bung.entity.BungEntity;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BungJpaRepository extends JpaRepository<BungEntity, Long> {
    Optional<BungEntity> findByBungId(String bungId);

    void deleteByBungId(String bungId);
}
