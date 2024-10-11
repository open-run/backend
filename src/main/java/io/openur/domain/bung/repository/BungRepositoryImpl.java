package io.openur.domain.bung.repository;

import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.user.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BungRepositoryImpl implements BungRepository, BungDAO {
    private final JPAQueryFactory queryFactory;
    private final BungJpaRepository bungJpaRepository;


    @Override
    public Page<Bung> findBungsWithStatus(User user, BungStatus status,
        Pageable pageable) {
        List<Bung> contents = queryFactory
            .selectDistinct(bungEntity)
            .from(bungEntity)
            .where(
                isAvailable(user, status)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(bungEntity.startDateTime.asc())
            .fetch().stream().map(Bung::from)
            .toList();

        JPAQuery<Long> count = queryFactory
            .selectDistinct(bungEntity.count())
            .from(bungEntity)
            .where(bungEntity.startDateTime.goe(LocalDateTime.now()));

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
    }

    @Override
    public Bung findByBungId(String bungId) {
        BungEntity bungEntity = bungJpaRepository.findByBungId(bungId)
            .orElseThrow(() -> new NoSuchElementException("Bung not found"));
        return Bung.from(bungEntity);
    }

    @Override
    public void deleteByBungId(String bungId) {
        bungJpaRepository.deleteByBungId(bungId);
    }

    @Override
    public Bung save(Bung bung) {
        return Bung.from(bungJpaRepository.save(bung.toEntity()));
    }

    private BooleanExpression isAvailable(User user, BungStatus status) {
        BooleanExpression baseCondition = bungEntity.startDateTime.goe(LocalDateTime.now());

        if(!BungStatus.AVAILABLE.equals(status)) return baseCondition;

        List<String> filterIds = queryFactory
            .selectDistinct(userBungEntity.bungEntity.bungId)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(
                userBungEntity.userEntity.eq(user.toEntity()),
                baseCondition
            ).fetch();

        return baseCondition.and(bungEntity.bungId.notIn(filterIds));
    }
}
