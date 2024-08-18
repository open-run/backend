package io.openur.domain.bung.repository.dao;

import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BungDAO {
    Page<BungDetailDto> findBungs(User user, boolean isParticipating, Pageable pageable);
}
