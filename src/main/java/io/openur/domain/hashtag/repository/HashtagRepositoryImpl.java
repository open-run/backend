package io.openur.domain.hashtag.repository;

import io.openur.domain.hashtag.entity.HashtagEntity;
import io.openur.domain.hashtag.model.Hashtag;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HashtagRepositoryImpl implements HashtagRepository {
    private final HashtagJpaRepository hashtagJpaRepository;

    @Override
    public Hashtag save(Hashtag hashtag) {
        Optional<HashtagEntity> existingHashtag = hashtagJpaRepository.findByHashtagStr(
            hashtag.getHashtagStr());
        return existingHashtag.map(Hashtag::from)
            .orElseGet(() -> Hashtag.from(hashtagJpaRepository.save(hashtag.toEntity())));
    }

    @Override
    public Optional<Hashtag> findByHashtagStr(String hashtagStr) {
        return hashtagJpaRepository.findByHashtagStr(hashtagStr)
            .map(Hashtag::from);
    }

    @Override
    public List<Hashtag> findByHashtagStrIn(List<String> hashtagStrs) {
        return hashtagJpaRepository.findByHashtagStrIn(hashtagStrs)
            .stream()
            .map(Hashtag::from)
            .collect(Collectors.toList());
    }

    @Override
    public List<Hashtag> findByHashtagIdIn(List<Long> hashtagIds) {
        return hashtagJpaRepository.findByHashtagIdIn(hashtagIds)
            .stream()
            .map(Hashtag::from)
            .collect(Collectors.toList());
    }
}
