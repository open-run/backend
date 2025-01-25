package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bung.model.Bung;
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

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void bulkInsertHashtags(Bung bung, List<Hashtag> hashtags) {
        for (Hashtag hashtag : hashtags) {
            BungHashtag bungHashtag = new BungHashtag(bung, hashtag);
            entityManager.persist(bungHashtag.toEntity());
        }
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public void updateHashtags(Bung bung, List<Hashtag> hashtags) {
        bungHashtagJpaRepository.deleteByBungEntity_BungId(bung.getBungId());
        bulkInsertHashtags(bung, hashtags);
    }

    @Override
    public List<Hashtag> findHashtagsByBungId(String bungId) {
        return bungHashtagJpaRepository.findByBungEntity_BungId(bungId).stream()
            .map(Hashtag::from).toList();
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
