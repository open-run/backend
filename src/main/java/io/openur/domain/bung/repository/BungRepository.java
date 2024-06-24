package io.openur.domain.bung.repository;

import io.openur.domain.bung.entity.BungEntity;

public interface BungRepository {

    BungEntity save(BungEntity bungEntity);

    BungEntity findByBungId(String bungId);
}
