package io.openur.domain.hashtag.repository;

import io.openur.domain.hashtag.model.Hashtag;
import java.util.List;
import java.util.Optional;

public interface HashtagRepository {

    List<Hashtag> saveAll(List<String> hashtagStrs);

    Optional<Hashtag> findByHashtagStr(String hashtagStr);

    List<Hashtag> findByHashtagStrIn(List<String> hashtagStrs);

    List<Hashtag> findByHashtagIdIn(List<Long> hashtagIds);

    List<Hashtag> findByHashtagStrContaining(String substring);
}
