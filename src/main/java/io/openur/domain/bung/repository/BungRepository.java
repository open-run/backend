package io.openur.domain.bung.repository;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BungRepository {
    Page<Bung> findBungsWithStatus(
        User user, boolean isAvailableOnly, Pageable pageable);

    Bung save(Bung bung);

    void deleteByBungId(String bungId);

}
