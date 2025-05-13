package io.openur.domain.hashtag.repository;

import io.openur.domain.hashtag.model.Hashtag;
import java.util.List;

public interface HashtagRepository {

    List<Hashtag> saveAll(List<String> hashtagStrs);
    
    List<Hashtag> findByHashtagStrIn(List<String> hashtagStrs);
    
    List<Hashtag> findByHashtagStrContaining(String substring);
}
