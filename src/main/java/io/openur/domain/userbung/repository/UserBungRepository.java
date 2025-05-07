package io.openur.domain.userbung.repository;

import com.querydsl.core.Tuple;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.model.UserBung;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserBungRepository {

    int countParticipantsByBungId(String bungId);

    Optional<BungInfoWithMemberListDto> findBungWithUsersById(String bungId);

    Page<Tuple> findAllFrequentUsers(List<String> bungIds, User user, Pageable pageable);

    Page<UserBung> findJoinedBungsByUserWithStatus(
        User user, Boolean isOwned, BungStatus status, Pageable pageable);

    List<String> findJoinedBungsId(User user);

    Boolean existsByUserIdAndBungId(String userId, String bungId);

    UserBung save(UserBung userBung);

    UserBung findByUserIdAndBungId(String userId, String bungId);

    UserBung findCurrentOwner(String bungId);

    void removeUserFromBung(UserBung userBung);

    void deleteByBungId(String bungId);
}
