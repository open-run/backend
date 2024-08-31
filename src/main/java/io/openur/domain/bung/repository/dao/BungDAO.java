package io.openur.domain.bung.repository.dao;

import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.user.model.User;
import io.openur.global.enums.BungStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BungDAO {
    Page<BungDetailDto> findBungs(User user, BungStatus status, Pageable pageable);
}
