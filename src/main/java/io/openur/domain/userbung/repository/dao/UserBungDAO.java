package io.openur.domain.userbung.repository.dao;

import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.user.dto.GetUsersResponseDto;
import io.openur.domain.user.model.User;
import io.openur.global.enums.BungStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserBungDAO {

    Page<GetUsersResponseDto> findAllFrequentUsers(User currentUser, Pageable pageable);

    Page<BungDetailDto> findAvailableBungs(String userId, Pageable pageable);

    Page<BungDetailDto> findBungs(String userId, BungStatus status, Pageable pageable);
}
