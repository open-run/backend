package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.hashtag.model.Hashtag;
import java.util.List;

public interface BungHashtagRepository {

    Bung saveNewBungHashtag(Bung bung, List<Hashtag> hashtags);
    
    void insertHashtagConnection(Bung bung, List<Hashtag> hashtags);
    
    List<Hashtag> findHashtagsByBungId(String bungId);

    void deleteByBungId(String bungId);

}
