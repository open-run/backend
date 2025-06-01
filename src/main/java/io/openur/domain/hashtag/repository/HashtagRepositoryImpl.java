package io.openur.domain.hashtag.repository;

import io.openur.domain.hashtag.entity.HashtagEntity;
import io.openur.domain.hashtag.model.Hashtag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HashtagRepositoryImpl implements HashtagRepository {

    private final HashtagJpaRepository hashtagJpaRepository;
    
    /**
     * 문자열 리스트를 받아 없는 경우 생성 & 있는 경우 조회해서 HashTag 목록을 반환합니다.
     * 빈 문자열 입력시 빈 리스트 반환
     * @return List<Hashtag></HashTag>
     */
    @Override
    public List<Hashtag> saveNotListedTags(List<String> hashtagStrings) {
        if(hashtagStrings.isEmpty()) return Collections.emptyList();
        
        // 이미 DB 에 존재하는 해시태그를 가져온다, 만일 전부 존재할 경우 하기 로직을 거칠 이유가 없다
        List<Hashtag> theExistHashtags = findByHashtagStrIn(hashtagStrings);
        if(hashtagStrings.size() == theExistHashtags.size()) {
            return theExistHashtags;
        }
        
        // set 으로 변환하고 contain 을 사용하는 것으로 간단하게 O(1) 필터가 가능하다
        Set<String> theExistStrings =  theExistHashtags.stream()
            .map(Hashtag::getHashtagStr)
            .collect(Collectors.toSet());
        
        // 등록된 해시태그를 필터하고, 새 entity 로 만들어 저장하고 dto 로 매핑
        List<HashtagEntity> toSave = hashtagStrings.stream()
            .filter(hashtag -> !theExistStrings.contains(hashtag))
            .map(Hashtag::new)
            .map(Hashtag::toEntity)
            .toList();
        
        List<Hashtag> theSaved = hashtagJpaRepository.saveAll(toSave)
            .stream().map(Hashtag::from).toList();
        
        // 기존에 존재하는 해시태그와 새로 등록한 해시태그들을 모두 반환
        List<Hashtag> allHashtags = new ArrayList<>(theExistHashtags);
        allHashtags.addAll(theSaved);
        
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
