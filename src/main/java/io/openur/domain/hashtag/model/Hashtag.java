package io.openur.domain.hashtag.model;

import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import io.openur.domain.hashtag.entity.HashtagEntity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Hashtag {
    @Getter
    private Long hashtagId;
    @Getter
    private String hashtagStr;
    
    private List<BungHashtagEntity> bungHashtags;

    public Hashtag(String hashtagStr) {
        this.hashtagStr = hashtagStr;
    }

//    public static Hashtag from(final BungHashtagEntity bungHashtagEntity) {
//        return new Hashtag(
//            bungHashtagEntity.getHashtagEntity().getHashtagId(),
//            bungHashtagEntity.getHashtagEntity().getHashtagStr(),
//
//        );
//    }

    public static Hashtag from(final HashtagEntity hashtagEntity) {
        return new Hashtag(
            hashtagEntity.getHashtagId(),
            hashtagEntity.getHashtagStr(),
            null // bungHashtags는 역방향 참조 — Hashtag 모델 내에서 사용되지 않으므로 lazy loading 방지
        );
    }

    public HashtagEntity toEntity() {
        return new HashtagEntity(
            this.hashtagId, this.hashtagStr, this.bungHashtags
        );
    }
    
}
