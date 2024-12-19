package io.openur.domain.bung.repository;

import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.model.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BungRepositoryImpl implements BungRepository {

    private final JPAQueryFactory queryFactory;
    private final BungJpaRepository bungJpaRepository;

    @Override
    public Page<Bung> findBungsWithStatus(User user, boolean isAvailableOnly,
        Pageable pageable) {
        List<Bung> contents = queryFactory
            .selectDistinct(bungEntity)
            .from(bungEntity)
            .where(
                isAvailable(user, isAvailableOnly)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(bungEntity.startDateTime.asc())
            .fetch().stream().map(Bung::from)
            .toList();

        JPAQuery<Long> count = queryFactory
            .selectDistinct(bungEntity.count())
            .from(bungEntity)
            .where(
                isAvailable(user, isAvailableOnly)
            );

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
    }

    @Override
    public void deleteByBungId(String bungId) {
        bungJpaRepository.deleteByBungId(bungId);
    }

    @Override
    public Bung save(Bung bung) {
        return Bung.from(bungJpaRepository.save(bung.toEntity()));
    }

    @Override
    public Boolean isBungStarted(String bungId) {
        Bung bung = Bung.from(bungJpaRepository.findBungEntityByBungId(bungId));
        return bung.getStartDateTime().isBefore(LocalDateTime.now());
    }

    private BooleanExpression isAvailable(User user, boolean isAvailableOnly) {
        // 기본적으로 Bung 은 행사 시작 이전것이 보여야함.
        BooleanExpression baseCondition = bungEntity.startDateTime.goe(LocalDateTime.now());

        if (!isAvailableOnly) {
            return baseCondition;
        }

        // 내가 이미 참여한 벙들의 ID
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
