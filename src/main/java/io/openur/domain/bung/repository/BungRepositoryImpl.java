package io.openur.domain.bung.repository;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.user.entity.QUserEntity.userEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    public Page<BungInfoWithMemberListDto> findBungsWithStatus(User user, boolean isAvailableOnly,
        Pageable pageable)
    {
        Map<BungEntity, List<UserBungEntity>> entries = queryFactory
            .select(userBungEntity, bungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .where(isAvailable(user, isAvailableOnly))
            .orderBy(
                bungEntity.startDateTime.asc(),
                isMyself(user),
                userBungEntity.isOwner.desc(),
                userBungEntity.userEntity.nickname.asc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .transform(groupBy(bungEntity).as(list(userBungEntity)));

        JPAQuery<Long> count = queryFactory
            .select(bungEntity.countDistinct())
            .from(bungEntity)
            .where(isAvailable(user, isAvailableOnly));

        List<BungInfoWithMemberListDto> contents = new ArrayList<>();
        for(Entry<BungEntity, List<UserBungEntity>> entry : entries.entrySet()) {
            contents.add(new BungInfoWithMemberListDto(entry));
        }

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
    }

    @Override
    public Bung findBungById(String bungId) {
        return Bung.from(bungJpaRepository.findBungEntityByBungId(bungId));
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

    private OrderSpecifier<?> isMyself(User user) {
        return new OrderSpecifier<>(Order.DESC,  // DESC로 변경하여 true가 먼저 오도록
            Expressions.booleanTemplate(
                "case when {0} = {1} then true else false end",
                userBungEntity.userEntity.userId,
                user.getUserId()
            ));
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
