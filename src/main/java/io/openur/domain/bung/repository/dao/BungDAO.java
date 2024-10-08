package io.openur.domain.bung.repository.dao;

import io.openur.domain.bung.dto.BungDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BungDAO {
    Page<BungDetailDto> findBungs(Pageable pageable);
    Page<BungDetailDto> findOwnedBungs(String userId, Pageable pageable);
}
