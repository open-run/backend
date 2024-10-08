package io.openur.domain.hashtag.model;

import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import io.openur.domain.hashtag.entity.HashtagEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Hashtag {
    private Long hashtagId;
    private String hashtagStr;

    public Hashtag(String hashtagStr) {
        this.hashtagStr = hashtagStr;
    }

    public static Hashtag from(final BungHashtagEntity bungHashtagEntity) {
        return new Hashtag(bungHashtagEntity.getHashtagEntity().getHashtagId(),
                bungHashtagEntity.getHashtagEntity().getHashtagStr());
    }

    public static Hashtag from(final HashtagEntity hashtagEntity) {
        return new Hashtag(hashtagEntity.getHashtagId(), hashtagEntity.getHashtagStr());
    }

    public HashtagEntity toEntity() {
        return new HashtagEntity(this.hashtagId, this.hashtagStr);
    }
}
