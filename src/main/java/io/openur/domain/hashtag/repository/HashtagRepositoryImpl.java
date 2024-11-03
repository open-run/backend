package io.openur.domain.hashtag.repository;

import io.openur.domain.hashtag.model.Hashtag;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HashtagRepositoryImpl implements HashtagRepository {
    private final HashtagJpaRepository hashtagJpaRepository;

    /**
     * Saves new hashtag strings into the database and returns all hashtag elements with their ID
     * values.
     *
     * @param hashtagStrs List of hashtag strings to save.
     * @return List of all hashtags with their ID values.
     */
    @Override
    public List<Hashtag> saveAll(List<String> hashtagStrs) {
        List<Hashtag> existingHashtags = hashtagJpaRepository.findByHashtagStrIn(hashtagStrs)
            .stream()
            .map(Hashtag::from)
            .toList();

        List<Hashtag> toSave = hashtagStrs.stream()
            .filter(hashtagStr -> existingHashtags.stream()
                .map(Hashtag::getHashtagStr)
                .noneMatch(hashtagStr::equals)
            )
            .map(Hashtag::new)
            .toList();

        List<Hashtag> saved = hashtagJpaRepository.saveAll(
                toSave.stream()
                .map(Hashtag::toEntity)
                .toList())
            .stream()
            .map(Hashtag::from)
            .toList();

        List<Hashtag> allHashtags = new ArrayList<>(existingHashtags);
        allHashtags.addAll(saved);
        return allHashtags;
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
