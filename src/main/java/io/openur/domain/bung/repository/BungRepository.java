package io.openur.domain.bung.repository;

import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.user.model.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BungRepository {

    Page<BungInfoWithMemberListDto> findBungs(
        User user, Pageable pageable);
    
    Page<BungInfoDto> findBungsWithLocation(
        String keyword, Pageable pageable);
    
    Page<BungInfoDto> findBungWithHashtag(
        String hashTag, Pageable pageable
    );
    
    Bung findBungById(String bungId);

    void deleteByBungId(String bungId);
    
    Bung save(Bung bung, List<BungHashtag> hashtags);
    
    Boolean isBungStarted(String bungId);
}
