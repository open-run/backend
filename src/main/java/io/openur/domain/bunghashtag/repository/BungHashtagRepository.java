package io.openur.domain.bunghashtag.repository;

import java.util.List;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.hashtag.model.Hashtag;

public interface BungHashtagRepository {
    BungHashtag save(BungHashtag bungHashtag);

    List<Hashtag> findBungHashtags(String bungId);

    List<Bung> findBungByHashtag(String hashtagStr);

    List<Bung> findBungByHashtags(List<String> hashtagStrs);

    void deleteByBungId(String bungId);

}
