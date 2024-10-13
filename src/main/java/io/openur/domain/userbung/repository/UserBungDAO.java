package io.openur.domain.userbung.repository;

import com.querydsl.core.Tuple;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.user.dto.GetUsersResponseDto;
import io.openur.domain.user.model.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserBungDAO {
    List<String> findJoinedBungsId(User user);
    Page<Bung> findBungsWithStatus(User user, BungStatus status, Pageable pageable);
    Page<Tuple> findAllFrequentUsers(List<String> bungIds, User user, Pageable pageable);
}
