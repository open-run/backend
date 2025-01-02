package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.hashtag.model.Hashtag;
import java.util.List;

public interface BungHashtagRepository {

    void bulkInsertHashtags(Bung bung, List<Hashtag> hashtags);

    List<Hashtag> findHashtagsByBungId(String bungId);

    List<Bung> findBungByHashtag(String hashtagStr);

    List<Bung> findBungByHashtags(List<String> hashtagStrs);

    void deleteByBungId(String bungId);

}
