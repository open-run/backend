package io.openur.domain.bung.repository;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.bunghashtag.entity.QBungHashtagEntity.bungHashtagEntity;
import static io.openur.domain.hashtag.entity.QHashtagEntity.hashtagEntity;
import static io.openur.domain.user.entity.QUserEntity.userEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.enums.GetBungResultEnum;
import io.openur.domain.bung.exception.GetBungException;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    public Page<BungInfoDto> findBungsWithLocation(String keyword, Pageable pageable) {

        // 공통 조건 추출
        BooleanExpression locationCondition = bungEntity.location.containsIgnoreCase(keyword);
        BooleanExpression dateCondition = bungEntity.startDateTime.gt(LocalDateTime.now());

        // 1. 단일 쿼리로 직접 조회
        List<BungEntity> bungs = queryFactory
            .selectFrom(bungEntity)
            .where(locationCondition, dateCondition)
            .orderBy(bungEntity.startDateTime.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 2. DTO 변환
        List<BungInfoDto> contents = bungs.stream()
            .map(Bung::from)
            .map(BungInfoDto::new)
            .toList();

        // 3. 카운트 쿼리 (동일한 조건 사용)
        JPAQuery<Long> countQuery = queryFactory
            .select(bungEntity.count())
            .from(bungEntity)
            .where(locationCondition, dateCondition);

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }
    
    @Override
    public Page<BungInfoDto> findBungWithHashtag(List<String> listOfTags, Pageable pageable) {
        // 입력 검증
        if (listOfTags == null || listOfTags.isEmpty()) {
            return Page.empty(pageable);
        }

        List<String> filteredTags = listOfTags.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .toList();

        if (filteredTags.isEmpty()) {
            return Page.empty(pageable);
        }

        // 공통 조건
        BooleanExpression hashtagCondition = hashtagEntity.hashtagStr.in(filteredTags);
        BooleanExpression dateCondition = bungEntity.startDateTime.gt(LocalDateTime.now());

        // 1. 페이징된 BungEntity ID 조회
        List<String> bungIds = queryFactory
            .selectDistinct(bungHashtagEntity.bungEntity.bungId)
            .from(bungHashtagEntity)
            .join(bungHashtagEntity.hashtagEntity, hashtagEntity)
            .join(bungHashtagEntity.bungEntity, bungEntity)
            .where(hashtagCondition, dateCondition)
            .orderBy(bungEntity.startDateTime.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (bungIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 2. 상세 데이터 조회
        List<BungEntity> bungs = queryFactory
            .selectFrom(bungEntity)
            .where(bungEntity.bungId.in(bungIds))
            .orderBy(bungEntity.startDateTime.asc())
            .fetch();

        // 3. DTO 변환
        List<BungInfoDto> contents = bungs.stream()
            .map(Bung::from)
            .map(BungInfoDto::new)
            .toList();

        // 4. 카운트 쿼리
        long total = queryFactory
            .select(bungEntity.bungId.countDistinct())
            .from(bungHashtagEntity)
            .join(bungHashtagEntity.hashtagEntity, hashtagEntity)
            .join(bungHashtagEntity.bungEntity, bungEntity)
            .where(hashtagCondition, dateCondition)
            .fetchOne();

        return new PageImpl<>(contents, pageable, total);
    }

    
    @Override
    public Bung findBungById(String bungId) {
        Optional<BungEntity> optionalBungEntity = bungJpaRepository
            .findBungEntityByBungId(bungId);
        
        if (optionalBungEntity.isEmpty()) {
            throw new GetBungException(GetBungResultEnum.BUNG_NOT_FOUND);
        }
        
        return Bung.from(optionalBungEntity.get());
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
        Bung bung = this.findBungById(bungId);
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
