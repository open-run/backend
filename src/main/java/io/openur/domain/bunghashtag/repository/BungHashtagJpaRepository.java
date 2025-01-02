package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BungHashtagJpaRepository extends JpaRepository<BungHashtagEntity, Long> {

    List<BungHashtagEntity> findByBungEntity_BungId(String bungId);

    List<BungEntity> findBungEntityByHashtagEntity_HashtagStr(String hashtagStr);

    List<BungEntity> findBungEntityByHashtagEntity_HashtagStrIn(List<String> hashtagStrs);

    void deleteByBungEntity_BungId(String bungId);

}
