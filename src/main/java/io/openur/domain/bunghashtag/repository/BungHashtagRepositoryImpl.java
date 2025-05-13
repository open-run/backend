package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.hashtag.model.Hashtag;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BungHashtagRepositoryImpl implements BungHashtagRepository {

    private final BungHashtagJpaRepository bungHashtagJpaRepository;
    private final BungRepositoryImpl bungRepository;
    
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
    public Bung updateBungHashtag(Bung bung, List<Hashtag> hashtags) {
        // Hashtags 중 연결관계가 존재하는 것만 돌아오는 목록
        List<BungHashtag> existBungHashTag =
            bungHashtagJpaRepository
                .findAllByBungEntity_BungIdAndHashtagEntity_HashtagStrIn(
                    bung.getBungId(),
                    hashtags.stream().map(Hashtag::getHashtagStr).toList()
                )
                .stream()
                .map(BungHashtag::from)
                .toList();
        // HashTag String 을 세트로
        Set<String> existBungHashtagStrSet = existBungHashTag.stream()
            .map(bungHashtag -> bungHashtag.getHashtag().getHashtagStr())
            .collect(Collectors.toSet());
        
        Bung finalBung = bung;
        // 기 존재 해시태그 연결관계를 제외하고 생성 및 저장합니다
        List<BungHashtag> newBungHashTag = bungHashtagJpaRepository
            .saveAll(
                hashtags.stream()
                    .filter(hashtag ->
                        !existBungHashtagStrSet.contains(hashtag.getHashtagStr())
                    )
                    .map(hashtag ->
                        new BungHashtag(finalBung, hashtag).toEntity()
                    )
                    .toList()
            )
            .stream().map(BungHashtag::from).toList();
        
        // 수정하려는 연결관계 목록
        List<BungHashtag> bungHashtags = new ArrayList<>(existBungHashTag);
        bungHashtags.addAll(newBungHashTag);
        
        // 목록을 통째로 교체 후 저장 리천
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
    public void deleteByBungId(String bungId) {
        bungHashtagJpaRepository.deleteByBungEntity_BungId(bungId);
    }
}
