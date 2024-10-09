package io.openur.domain.userbung.repository;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserBungDAO {
    Page<Bung> findBungsWithStatus(User user, BungStatus status, Pageable pageable);
}
