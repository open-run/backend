package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.hashtag.entity.HashtagEntity;
import io.openur.domain.hashtag.model.Hashtag;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class BungHashtagRepositoryImpl implements BungHashtagRepository {

    private final BungHashtagJpaRepository bungHashtagJpaRepository;
    private final BungRepositoryImpl bungRepository;
    private final EntityManager entityManager;
    
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
        
        bungRepository.save(bung, bungHashtags);
        
        return bungRepository.findBungById(bung.getBungId());
    }
    
    @Override
    @Transactional
    public void insertHashtagConnection(Bung bung, List<Hashtag> newHashtags) {
        // 2. 기존/신규 해시태그 문자열 세트
        Set<String> existingStrs = bungHashtagJpaRepository
            .findAllByBungEntity_BungId(bung.getBungId()).stream()
            .map(BungHashtagEntity::getHashtagEntity)
            .map(HashtagEntity::getHashtagStr)
            .collect(Collectors.toSet());
        
        Set<String> newStrs = newHashtags.stream()
            .map(Hashtag::getHashtagStr)
            .collect(Collectors.toSet());
        
        bungHashtagJpaRepository.
            deleteAllByBungEntity_BungIdAndHashtagEntity_HashtagStrNotIn(
                bung.getBungId(), newStrs
            );
        
        entityManager.flush();
        entityManager.clear();
        
        // 4. 추가 대상 처리 (벌크 연산)
        List<BungHashtagEntity> toSave = newHashtags.stream()
            .filter(h -> !existingStrs.contains(h.getHashtagStr()))
            .map(h -> new BungHashtag(bung, h))
            .map(BungHashtag::toEntity)
            .toList();
        
        bungHashtagJpaRepository.saveAll(toSave);
    }

    @Override
    public List<Hashtag> findHashtagsByBungId(String bungId) {
        return bungHashtagJpaRepository.findByBungEntity_BungId(bungId).stream()
            .map(BungHashtagEntity::getHashtagEntity)
            .map(Hashtag::from)
            .toList();
    }

    @Override
    public void deleteByBungId(String bungId) {
        bungHashtagJpaRepository.deleteByBungEntity_BungId(bungId);
    }
}
