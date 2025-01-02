package io.openur.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserEntity is a Querydsl query type for UserEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserEntity extends EntityPathBase<UserEntity> {

    public static final QUserEntity userEntity = new QUserEntity("userEntity");
    private static final long serialVersionUID = -1401535967L;
    public final BooleanPath blacklisted = createBoolean("blacklisted");

    public final StringPath blockchainAddress = createString("blockchainAddress");

    public final DateTimePath<java.time.LocalDateTime> createdDate = createDateTime("createdDate",
        java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final BooleanPath identityAuthenticated = createBoolean("identityAuthenticated");

    public final DateTimePath<java.time.LocalDateTime> lastLoginDate = createDateTime("lastLoginDate",
        java.time.LocalDateTime.class);

    public final StringPath nickname = createString("nickname");

    public final EnumPath<io.openur.domain.user.model.Provider> provider = createEnum("provider",
        io.openur.domain.user.model.Provider.class);

    public final NumberPath<Integer> runningFrequency = createNumber("runningFrequency", Integer.class);

    public final StringPath runningPace = createString("runningPace");

    public final StringPath userId = createString("userId");

    public QUserEntity(String variable) {
        super(UserEntity.class, forVariable(variable));
    }

    public QUserEntity(Path<? extends UserEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserEntity(PathMetadata metadata) {
        super(UserEntity.class, metadata);
    }

}

