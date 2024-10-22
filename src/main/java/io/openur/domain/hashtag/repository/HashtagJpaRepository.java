package io.openur.domain.hashtag.repository;

import io.openur.domain.hashtag.entity.HashtagEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagJpaRepository extends JpaRepository<HashtagEntity, Long> {
    Optional<HashtagEntity> findByHashtagStr(String hashtagStr);

    List<HashtagEntity> findByHashtagStrIn(List<String> hashtagStrs);

    List<HashtagEntity> findByHashtagIdIn(List<Long> hashtagIds);

    List<HashtagEntity> findByHashtagStrContaining(String substring);
}
