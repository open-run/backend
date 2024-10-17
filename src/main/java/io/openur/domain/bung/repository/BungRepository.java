package io.openur.domain.bung.repository;

import io.openur.domain.bung.model.Bung;

public interface BungRepository {

    Bung save(Bung bung);

    void deleteByBungId(String bungId);
}
