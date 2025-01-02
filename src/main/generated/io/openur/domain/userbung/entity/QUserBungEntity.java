package io.openur.domain.userbung.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserBungEntity is a Querydsl query type for UserBungEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserBungEntity extends EntityPathBase<UserBungEntity> {

    public static final QUserBungEntity userBungEntity = new QUserBungEntity("userBungEntity");
    private static final long serialVersionUID = 1570483417L;
    private static final PathInits INITS = PathInits.DIRECT2;
    public final io.openur.domain.bung.entity.QBungEntity bungEntity;

    public final BooleanPath isOwner = createBoolean("isOwner");

    public final DateTimePath<java.time.LocalDateTime> modifiedAt = createDateTime("modifiedAt",
        java.time.LocalDateTime.class);

    public final BooleanPath participationStatus = createBoolean("participationStatus");

    public final NumberPath<Long> userBungId = createNumber("userBungId", Long.class);

    public final io.openur.domain.user.entity.QUserEntity userEntity;

    public QUserBungEntity(String variable) {
        this(UserBungEntity.class, forVariable(variable), INITS);
    }

    public QUserBungEntity(Path<? extends UserBungEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserBungEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserBungEntity(PathMetadata metadata, PathInits inits) {
        this(UserBungEntity.class, metadata, inits);
    }

    public QUserBungEntity(Class<? extends UserBungEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.bungEntity =
            inits.isInitialized("bungEntity") ? new io.openur.domain.bung.entity.QBungEntity(forProperty("bungEntity"))
                : null;
        this.userEntity =
            inits.isInitialized("userEntity") ? new io.openur.domain.user.entity.QUserEntity(forProperty("userEntity"))
                : null;
    }

}

