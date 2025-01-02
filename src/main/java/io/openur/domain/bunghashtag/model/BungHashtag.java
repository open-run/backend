package io.openur.domain.bunghashtag.model;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import io.openur.domain.hashtag.model.Hashtag;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BungHashtag {

    private Long bungHashtagId;
    private Bung bung;
    private Hashtag hashtag;

    public BungHashtag(Bung bung, Hashtag hashtag) {
        this.bung = bung;
        this.hashtag = hashtag;
    }

    public static BungHashtag from(final BungHashtagEntity bungHashtagEntity) {
        return new BungHashtag(bungHashtagEntity.getBungHashtagId(),
            Bung.from(bungHashtagEntity.getBungEntity()),
            Hashtag.from(bungHashtagEntity.getHashtagEntity()));
    }

    public BungHashtagEntity toEntity() {
        return new BungHashtagEntity(this.bungHashtagId, this.bung.toEntity(),
            this.hashtag.toEntity());
    }


}
