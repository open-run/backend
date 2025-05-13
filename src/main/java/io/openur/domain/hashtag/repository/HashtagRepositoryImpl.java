package io.openur.domain.hashtag.repository;

import io.openur.domain.hashtag.model.Hashtag;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HashtagRepositoryImpl implements HashtagRepository {

    private final HashtagJpaRepository hashtagJpaRepository;
    
    /**
     * Saves new hashtag strings into the database and returns all hashtag elements with their ID values.
     *
     * @param hashtagStrings List of hashtag strings to save.
     * @return List of all hashtags with their ID values.
     */
    @Override
    public List<Hashtag> saveAll(List<String> hashtagStrings) {
        // 이미 DB 에 존재하는 해시태그를 가져온다, 만일 전부 존재할 경우 하기 로직을 거칠 이유가 없다
        List<Hashtag> existingHashtags = findByHashtagStrIn(hashtagStrings);
        if(hashtagStrings.size() == existingHashtags.size()) {
            return existingHashtags;
        }
        
        // set 으로 변환하고 contain 을 사용하는 것으로 간단하게 O(1) 필터가 가능하다
        Set<String> existingStringSet =  existingHashtags.stream()
            .map(Hashtag::getHashtagStr)
            .collect(Collectors.toSet());
        
        // 등록된 해시태그를 필터하고, 새 entity 로 만들어 저장하고 dto 로 매핑
        List<Hashtag> savedHashTags = hashtagJpaRepository.saveAll(
            hashtagStrings.stream()
                .filter(hashtag -> !existingStringSet.contains(hashtag))
                .map(Hashtag::new)
                .map(Hashtag::toEntity)
                .toList()
        ).stream().map(Hashtag::from).toList();
        
        // 기존에 존재하는 해시태그와 새로 등록한 해시태그들을 모두 반환
        List<Hashtag> allHashtags = new ArrayList<>(existingHashtags);
        allHashtags.addAll(savedHashTags);
        
        return allHashtags;
    }
    
    @Override
    public List<Hashtag> findByHashtagStrIn(List<String> hashTagStrings) {
        return hashtagJpaRepository.findByHashtagStrIn(hashTagStrings)
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
