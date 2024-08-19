package io.openur.domain.userbung.repository.dao;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.dto.GetUsersResponseDto;
import io.openur.domain.user.model.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserBungDAO {
    List<Bung> findAllJoinBungsByUser(User user);

    Page<GetUsersResponseDto> findAllFrequentUsers(List<Bung> bungs,
        User currentUser, Pageable pageable);
}
