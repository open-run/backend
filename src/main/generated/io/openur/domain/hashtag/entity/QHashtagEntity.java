package io.openur.domain.hashtag.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QHashtagEntity is a Querydsl query type for HashtagEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHashtagEntity extends EntityPathBase<HashtagEntity> {

    private static final long serialVersionUID = 1136018289L;

    public static final QHashtagEntity hashtagEntity = new QHashtagEntity("hashtagEntity");

    public final NumberPath<Long> hashtagId = createNumber("hashtagId", Long.class);

    public final StringPath hashtagStr = createString("hashtagStr");

    public QHashtagEntity(String variable) {
        super(HashtagEntity.class, forVariable(variable));
    }

    public QHashtagEntity(Path<? extends HashtagEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHashtagEntity(PathMetadata metadata) {
        super(HashtagEntity.class, metadata);
    }

}

