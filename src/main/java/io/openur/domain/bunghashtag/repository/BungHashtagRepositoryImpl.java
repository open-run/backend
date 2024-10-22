package io.openur.domain.bunghashtag.repository;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.hashtag.model.Hashtag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BungHashtagRepositoryImpl implements BungHashtagRepository {
    private final BungHashtagJpaRepository bungHashtagJpaRepository;

    @Override
    public BungHashtag save(BungHashtag bungHashtag) {
        return BungHashtag.from(bungHashtagJpaRepository.save(bungHashtag.toEntity()));
    }

    @Override
    public List<Hashtag> findHashtagsByBungId(String bungId) {
        return bungHashtagJpaRepository.findByBungEntity_BungId(bungId).stream()
                .map(Hashtag::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<Bung> findBungByHashtag(String hashtagStr) {
        return bungHashtagJpaRepository.findBungEntityByHashtagEntity_HashtagStr(hashtagStr)
                .stream().map(Bung::from).collect(Collectors.toList());
    }

    @Override
    public List<Bung> findBungByHashtags(List<String> hashtagStrs) {
        return bungHashtagJpaRepository.findBungEntityByHashtagEntity_HashtagStrIn(hashtagStrs)
                .stream().map(Bung::from).collect(Collectors.toList());
    }

    @Override
    public void deleteByBungId(String bungId) {
        bungHashtagJpaRepository.deleteByBungEntity_BungId(bungId);
    }
}
