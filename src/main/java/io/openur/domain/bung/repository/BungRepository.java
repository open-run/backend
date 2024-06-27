package io.openur.domain.bung.repository;

import io.openur.domain.bung.model.Bung;

public interface BungRepository {

    Bung save(Bung bung);

    Bung findByBungId(String bungId);

    void deleteByBungId(String bungId);
}
