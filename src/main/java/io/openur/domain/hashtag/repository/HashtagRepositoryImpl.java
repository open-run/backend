package io.openur.domain.hashtag.repository;

import io.openur.domain.hashtag.entity.HashtagEntity;
import io.openur.domain.hashtag.model.Hashtag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HashtagRepositoryImpl implements HashtagRepository {
    private final HashtagJpaRepository hashtagJpaRepository;

    @Override
    public List<Hashtag> saveAll(List<String> hashtagStrs) {
        List<String> existingHashtags = hashtagJpaRepository.findByHashtagStrIn(hashtagStrs)
            .stream()
            .map(HashtagEntity::getHashtagStr)
            .toList();

        List<Hashtag> toSave = hashtagStrs.stream()
            .filter(hashtagStr -> !existingHashtags.contains(hashtagStr))
            .map(Hashtag::new)
            .toList();

        return hashtagJpaRepository.saveAll(
                toSave.stream()
                .map(Hashtag::toEntity)
                .toList())
            .stream()
            .map(Hashtag::from)
            .toList();
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
            .toList();
    }

    @Override
    public List<Hashtag> findByHashtagIdIn(List<Long> hashtagIds) {
        return hashtagJpaRepository.findByHashtagIdIn(hashtagIds)
            .stream()
            .map(Hashtag::from)
            .toList();
    }

    @Override
    public List<Hashtag> findByHashtagStrContaining(String substring) {
        return hashtagJpaRepository.findByHashtagStrContaining(substring)
            .stream()
            .map(Hashtag::from)
            .toList();
    }
}
