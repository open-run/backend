package io.openur.domain.bunghashtag.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBungHashtagEntity is a Querydsl query type for BungHashtagEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBungHashtagEntity extends EntityPathBase<BungHashtagEntity> {

    private static final long serialVersionUID = -1358491567L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBungHashtagEntity bungHashtagEntity = new QBungHashtagEntity("bungHashtagEntity");

    public final io.openur.domain.bung.entity.QBungEntity bungEntity;

    public final NumberPath<Long> bungHashtagId = createNumber("bungHashtagId", Long.class);

    public final io.openur.domain.hashtag.entity.QHashtagEntity hashtagEntity;

    public QBungHashtagEntity(String variable) {
        this(BungHashtagEntity.class, forVariable(variable), INITS);
    }

    public QBungHashtagEntity(Path<? extends BungHashtagEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBungHashtagEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBungHashtagEntity(PathMetadata metadata, PathInits inits) {
        this(BungHashtagEntity.class, metadata, inits);
    }

    public QBungHashtagEntity(Class<? extends BungHashtagEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.bungEntity = inits.isInitialized("bungEntity") ? new io.openur.domain.bung.entity.QBungEntity(forProperty("bungEntity")) : null;
        this.hashtagEntity = inits.isInitialized("hashtagEntity") ? new io.openur.domain.hashtag.entity.QHashtagEntity(forProperty("hashtagEntity")) : null;
    }

}

