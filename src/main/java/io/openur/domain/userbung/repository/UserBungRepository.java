package io.openur.domain.userbung.repository;

import com.querydsl.core.Tuple;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.enums.BungStatus;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.model.UserBung;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserBungRepository {

    int countParticipantsByBungId(String bungId);

    long countCurrentOwnedBungsByUserId(String userId);

    Optional<BungInfoWithMemberListDto> findBungWithUsersById(String bungId);

    Page<Tuple> findAllFrequentUsers(List<String> bungIds, User user, Pageable pageable);
    
    Page<UserBung> findJoinedBungsByUserWithStatus(
        User user, Boolean isOwned, BungStatus status, Boolean feedbackPending, Pageable pageable);

    List<String> findJoinedBungsId(User user);

    List<String> findMemberUserIdsByBungId(String bungId);

    Boolean existsByUserIdAndBungId(String userId, String bungId);

    UserBung save(UserBung userBung);

    /**
     * 참여 인증을 조건부 UPDATE로 처리한다.
     * @return 미인증 → 인증으로 실제 전환됐으면 true, 이미 인증 상태였으면 false
     */
    boolean confirmParticipation(String userId, String bungId);

    UserBung findByUserIdAndBungId(String userId, String bungId);

    boolean markFeedbackSubmittedIfPending(String userId, String bungId);

    UserBung findCurrentOwner(String bungId);

    void removeUserFromBung(UserBung userBung);

    void deleteByBungId(String bungId);
}
