package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.hashtag.model.Hashtag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BungHashtagRepositoryImpl implements BungHashtagRepository {

    private final BungHashtagJpaRepository bungHashtagJpaRepository;
    private final BungRepositoryImpl bungRepository;

    @PersistenceContext
    private EntityManager entityManager;
    
    /***
     * Bung 의 변동된 해시태그를 저장합니다
     * 목록이 빈 경우 제거 혹은 아무 동작도 않고 종료합니다.
     * @param bung
     * @param hashtags
     * @return Bung
     */
    @Override
    public Bung saveNewBungHashtag(Bung bung, List<Hashtag> hashtags) {
        if(hashtags.isEmpty()) return bung;
        
        Bung finalBung = bung;
        List<BungHashtag> bungHashtags = bungHashtagJpaRepository
            .saveAll(
                hashtags.stream().map(hashtag ->
                        new BungHashtag(finalBung, hashtag).toEntity()
                ).toList()
        ).stream().map(BungHashtag::from).toList();
        
        bung = bungRepository.save(bung, bungHashtags);
        return bung;
    }

    @Override
    public void updateHashtags(Bung bung, List<Hashtag> hashtags) {
        bungHashtagJpaRepository.deleteByBungEntity_BungId(bung.getBungId());
//        saveBungHashtag(bung, hashtags);
    }

    @Override
    public List<Hashtag> findHashtagsByBungId(String bungId) {
        return bungHashtagJpaRepository.findByBungEntity_BungId(bungId).stream()
            .map(BungHashtagEntity::getHashtagEntity)
            .map(Hashtag::from)
            .toList();
    }

    @Override
    public List<Bung> findBungByHashtag(String hashtagStr) {
        return bungHashtagJpaRepository.findBungEntityByHashtagEntity_HashtagStr(hashtagStr)
            .stream().map(Bung::from).toList();
    }

    @Override
    public List<Bung> findBungByHashtags(List<String> hashtagStrs) {
        return bungHashtagJpaRepository.findBungEntityByHashtagEntity_HashtagStrIn(hashtagStrs)
            .stream().map(Bung::from).toList();
    }

    @Override
    public void deleteByBungId(String bungId) {
        bungHashtagJpaRepository.deleteByBungEntity_BungId(bungId);
    }
}
