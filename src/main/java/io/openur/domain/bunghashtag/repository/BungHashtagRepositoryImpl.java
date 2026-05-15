package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepository;
import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
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
@Transactional(readOnly = true)
public class BungHashtagRepositoryImpl implements BungHashtagRepository {

    private final BungHashtagJpaRepository bungHashtagJpaRepository;
    private final BungRepository bungRepository;
    private final EntityManager entityManager;
    
    @Override
    @Transactional
    public Bung saveNewBungHashtag(Bung bung, List<Hashtag> hashtags) {
        if(hashtags.isEmpty()) return bung;

        BungEntity bungRef = entityManager.getReference(BungEntity.class, bung.getBungId());
        bungHashtagJpaRepository.saveAll(
            hashtags.stream()
                .map(hashtag -> new BungHashtagEntity(
                    null,
                    bungRef,
                    entityManager.getReference(HashtagEntity.class, hashtag.getHashtagId())
                ))
                .toList()
        );

        // saveAll 후 findBungById 의 query 트리거 시 JPA AUTO flush 모드가 자동 flush.
        // clear() 는 호출자 트랜잭션의 다른 영속 entity 까지 detach 시키므로 제거.
        return bungRepository.findBungById(bung.getBungId());
    }
    
    @Override
    @Transactional
    public void insertHashtagConnection(Bung bung, List<Hashtag> hashTags) {
        if (hashTags.isEmpty()) return;

        // 기존/신규 해시태그 문자열 세트
        Set<String> existingStrs = bungHashtagJpaRepository
            .findAllByBungEntity_BungId(bung.getBungId()).stream()
            .map(BungHashtagEntity::getHashtagEntity)
            .map(HashtagEntity::getHashtagStr)
            .collect(Collectors.toSet());

        Set<String> newStrs = hashTags.stream()
            .map(Hashtag::getHashtagStr)
            .collect(Collectors.toSet());

        bungHashtagJpaRepository
            .deleteAllByBungEntity_BungIdAndHashtagEntity_HashtagStrNotIn(
                bung.getBungId(), newStrs
            );

        // @Modifying JPQL DELETE 는 즉시 DB 반영. 후속 saveAll INSERT 와 키 충돌 없음.
        // 별도 flush/clear 불필요 — clear 는 호출자 영속성 컨텍스트 영향이라 제거.

        // 추가 대상 처리 (벌크 연산): 비영속 객체 생성 없이 em.getReference 로 프록시 참조
        BungEntity bungRef = entityManager.getReference(BungEntity.class, bung.getBungId());
        List<BungHashtagEntity> toSave = hashTags.stream()
            .filter(h -> !existingStrs.contains(h.getHashtagStr()))
            .map(h -> new BungHashtagEntity(
                null,
                bungRef,
                entityManager.getReference(HashtagEntity.class, h.getHashtagId())
            ))
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
    @Transactional
    public void deleteByBungId(String bungId) {
        bungHashtagJpaRepository.deleteByBungEntity_BungId(bungId);
    }
}
