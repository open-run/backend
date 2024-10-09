package io.openur.domain.bunghashtag.repository;

import java.util.List;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bunghashtag.model.BungHashtag;

public interface BungHashtagRepository {
    BungHashtag save(BungHashtag bungHashtag);

    List<String> findHashtagStrsByBungId(String bungId);

    List<Bung> findBungByHashtag(String hashtagStr);

    List<Bung> findBungByHashtags(List<String> hashtagStrs);

    void deleteByBungId(String bungId);

}
