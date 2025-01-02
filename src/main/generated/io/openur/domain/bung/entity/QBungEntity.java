package io.openur.domain.bung.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBungEntity is a Querydsl query type for BungEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBungEntity extends EntityPathBase<BungEntity> {

    public static final QBungEntity bungEntity = new QBungEntity("bungEntity");
    private static final long serialVersionUID = -2143999453L;
    public final StringPath afterRunDescription = createString("afterRunDescription");

    public final StringPath bungId = createString("bungId");

    public final StringPath description = createString("description");

    public final NumberPath<Float> distance = createNumber("distance", Float.class);

    public final DateTimePath<java.time.LocalDateTime> endDateTime = createDateTime("endDateTime",
        java.time.LocalDateTime.class);

    public final BooleanPath hasAfterRun = createBoolean("hasAfterRun");

    public final StringPath location = createString("location");

    public final NumberPath<Integer> memberNumber = createNumber("memberNumber", Integer.class);

    public final StringPath name = createString("name");

    public final StringPath pace = createString("pace");

    public final DateTimePath<java.time.LocalDateTime> startDateTime = createDateTime("startDateTime",
        java.time.LocalDateTime.class);

    public QBungEntity(String variable) {
        super(BungEntity.class, forVariable(variable));
    }

    public QBungEntity(Path<? extends BungEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBungEntity(PathMetadata metadata) {
        super(BungEntity.class, metadata);
    }

}

