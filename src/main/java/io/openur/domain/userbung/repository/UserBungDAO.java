package io.openur.domain.userbung.repository;

import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.model.UserBung;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserBungDAO {
    BungDetailDto findJoinedUsersByBungId(String bungId);
    Page<Bung> findJoinedBungsByUserWithStatus(User user, BungStatus status, Pageable pageable);
}
