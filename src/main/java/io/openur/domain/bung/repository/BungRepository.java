package io.openur.domain.bung.repository;

import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.enums.BungSearchCategory;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.user.model.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BungRepository {

    Page<BungInfoWithMemberListDto> findBungs(
        User user, Pageable pageable);

    Page<BungInfoWithMemberListDto> searchBungs(
        BungSearchCategory category, String keyword, Pageable pageable);
    
    Bung findBungById(String bungId);

    void deleteByBungId(String bungId);

    void setAsFaded(List<Bung> bungs);
    
    Bung save(Bung bung, List<BungHashtag> hashtags);

    void updateBung(Bung bung);

    /**
     * 미완료 상태일 때만 완료로 전환한다.
     * @return 이 호출이 실제로 완료 처리했으면 true, 이미 완료된 벙이면 false
     */
    boolean setAsCompletedIfPending(String bungId);

    Boolean isBungStarted(String bungId);

    Page<Bung> findBungsPastStartWithSingleParticipant(Pageable pageable);
}
