package io.openur.domain.bung.repository;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.user.entity.QUserEntity.userEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    public Page<BungInfoWithMemberListDto> findBungsWithStatus(
        User user, boolean isJoinedOnly, Pageable pageable)
    {
        // 대용량 객체 조회 고려 배치 사이징
        List<String> bungIds = queryFactory
            .select(bungEntity.bungId)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .where(isAvailable(user, isJoinedOnly))
            .orderBy(conditionalBungOrdering(user))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        
        // 배치 사이즈 내 조인 검색 최소화
        Map<BungEntity, List<UserBungEntity>> bungMap = !bungIds.isEmpty() ?
            queryFactory
                .selectFrom(userBungEntity)
                .join(userBungEntity.bungEntity, bungEntity).fetchJoin()
                .join(userBungEntity.userEntity, userEntity).fetchJoin()
                .where(userBungEntity.bungEntity.bungId.in(bungIds))
                .transform(groupBy(bungEntity).as(list(userBungEntity)))
            : Collections.emptyMap();
        
        List<BungInfoWithMemberListDto> contents = bungMap.entrySet().stream()
            .map(entry ->
                new BungInfoWithMemberListDto(entry.getKey(), entry.getValue())
            )
            .sorted(
                Comparator.comparing(dto -> bungIds.indexOf(dto.getBungId()))
            ).toList();
        
        JPAQuery<Long> countQuery = queryFactory
            .select(bungEntity.countDistinct())
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(isAvailable(user, isJoinedOnly));
        
        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }
    
    @Override
    public Page<BungInfoWithMemberListDto> findBungsWithLocation(
        String keyword, Pageable pageable) {
        
        List<String> bungIds = queryFactory
            .select(bungEntity.bungId)
            .from(bungEntity)
            .where(
                bungEntity.location.containsIgnoreCase(keyword),
                bungEntity.startDateTime.gt(LocalDateTime.now())
            )
            .orderBy(bungEntity.startDateTime.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        
        List<BungInfoWithMemberListDto> contents = !bungIds.isEmpty() ?
            queryFactory
                .selectFrom(bungEntity)
                .where(bungEntity.bungId.in(bungIds))
                .orderBy(bungEntity.startDateTime.asc())
                .fetch().stream().map(BungInfoWithMemberListDto::new).toList()
            : Collections.emptyList();
        
        JPAQuery<Long> countQuery = queryFactory
            .select(bungEntity.count())
            .from(bungEntity)
            .where(bungEntity.location.containsIgnoreCase(keyword));
        
        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
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
    public Bung save(Bung bung, List<BungHashtag> bungHashtags) {
        return Bung.from(bungJpaRepository.save(bung.toEntity(bungHashtags)));
    }

    @Override
    public Boolean isBungStarted(String bungId) {
        Bung bung = Bung.from(bungJpaRepository.findBungEntityByBungId(bungId));
        return bung.getStartDateTime().isBefore(LocalDateTime.now());
    }

    private OrderSpecifier[] conditionalBungOrdering(User user) {
        return new OrderSpecifier[] {
            bungEntity.startDateTime.asc(),
            new CaseBuilder()
                .when(
                    userBungEntity.userEntity.userId.eq(user.getUserId())
                )
                .then(0)
                .otherwise(1).asc(),
            userBungEntity.isOwner.desc(),
            userEntity.nickname.asc()
        };
    }

    private BooleanExpression isAvailable(User user, boolean isJoinedOnly) {
        // 기본적으로 Bung 은 행사 시작 이전것이 보여야함.
        BooleanExpression baseCondition = bungEntity.startDateTime.gt(LocalDateTime.now());

        if (!isJoinedOnly) {
            return baseCondition;
        }

        // 내가 이미 참여한 벙들의 ID
        List<String> joinedBungIds = queryFactory
            .select(userBungEntity.bungEntity.bungId)
            .from(userBungEntity)
            .where(userBungEntity.userEntity.eq(user.toEntity()))
            .fetch();
        
        BooleanExpression notJoined = joinedBungIds.isEmpty() ?
            null : bungEntity.bungId.notIn(joinedBungIds);
        
        return baseCondition.and(notJoined);
    }
}
