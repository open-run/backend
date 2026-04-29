package io.openur.domain.bung.repository;

import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.bunghashtag.entity.QBungHashtagEntity.bungHashtagEntity;
import static io.openur.domain.hashtag.entity.QHashtagEntity.hashtagEntity;
import static io.openur.domain.user.entity.QUserEntity.userEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.enums.BungSearchCategory;
import io.openur.domain.bung.enums.GetBungResultEnum;
import io.openur.domain.bung.exception.GetBungException;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.user.model.User;
import io.openur.domain.user.service.UserProfileImageUrlResolver;
import io.openur.domain.userbung.entity.UserBungEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    private final UserProfileImageUrlResolver userProfileImageUrlResolver;

    @Override
    public Page<BungInfoWithMemberListDto> findBungs(
        User user, Pageable pageable) {

        // 사용자가 참여한 벙 ID 조회 (한 번만 실행)
        Set<String> joinedBungIds = getJoinedBungIds(user);

        // 페이징된 벙 ID 조회 (정렬 순서 보존을 위한 LinkedHashMap 사용)
        List<String> bungIds = queryFactory
            .select(bungEntity.bungId)
            .from(bungEntity)
            .where(isAvailable(joinedBungIds))
            .orderBy(bungEntity.startDateTime.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (bungIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 배치 조회 및 DTO 변환
        List<BungInfoWithMemberListDto> contents = fetchBungInfoWithMembers(bungIds);

        // 카운트 쿼리 (lazy 평가 - 마지막 페이지가 아닐 때만 실행)
        JPAQuery<Long> countQuery = queryFactory
            .select(bungEntity.countDistinct())
            .from(bungEntity)
            .where(isAvailable(joinedBungIds));

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<BungInfoWithMemberListDto> searchBungs(
        BungSearchCategory category,
        String keyword,
        Pageable pageable
    ) {
        if (!StringUtils.hasText(keyword)) {
            return Page.empty(pageable);
        }

        String searchKeyword = normalizeKeyword(category, keyword);
        if (!StringUtils.hasText(searchKeyword)) {
            return Page.empty(pageable);
        }

        long total = countSearchBungs(category, searchKeyword);
        if (total == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<String> bungIds = searchBungIds(category, searchKeyword, pageable);
        if (bungIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        List<BungInfoWithMemberListDto> contents = fetchBungInfoWithMembers(bungIds);

        return new PageImpl<>(contents, pageable, total);
    }

    private long countSearchBungs(BungSearchCategory category, String keyword) {
        Long count = switch (category) {
            case NAME -> queryFactory
                .select(bungEntity.bungId.countDistinct())
                .from(bungEntity)
                .where(bungEntity.name.containsIgnoreCase(keyword), isSearchable())
                .fetchOne();
            case LOCATION -> queryFactory
                .select(bungEntity.bungId.countDistinct())
                .from(bungEntity)
                .where(bungEntity.location.containsIgnoreCase(keyword), isSearchable())
                .fetchOne();
            case HASHTAG -> queryFactory
                .select(bungHashtagEntity.bungEntity.bungId.countDistinct())
                .from(bungHashtagEntity)
                .join(bungHashtagEntity.hashtagEntity, hashtagEntity)
                .join(bungHashtagEntity.bungEntity, bungEntity)
                .where(hashtagEntity.hashtagStr.containsIgnoreCase(keyword), isSearchable())
                .fetchOne();
            case MEMBER -> queryFactory
                .select(userBungEntity.bungEntity.bungId.countDistinct())
                .from(userBungEntity)
                .join(userBungEntity.bungEntity, bungEntity)
                .join(userBungEntity.userEntity, userEntity)
                .where(userEntity.nickname.containsIgnoreCase(keyword), isSearchable())
                .fetchOne();
        };

        return Optional.ofNullable(count).orElse(0L);
    }

    private List<String> searchBungIds(BungSearchCategory category, String keyword, Pageable pageable) {
        return switch (category) {
            case NAME -> queryFactory
                .select(bungEntity.bungId)
                .from(bungEntity)
                .where(bungEntity.name.containsIgnoreCase(keyword), isSearchable())
                .orderBy(bungEntity.startDateTime.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
            case LOCATION -> queryFactory
                .select(bungEntity.bungId)
                .from(bungEntity)
                .where(bungEntity.location.containsIgnoreCase(keyword), isSearchable())
                .orderBy(bungEntity.startDateTime.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
            case HASHTAG -> queryFactory
                .select(bungEntity.bungId)
                .from(bungHashtagEntity)
                .join(bungHashtagEntity.hashtagEntity, hashtagEntity)
                .join(bungHashtagEntity.bungEntity, bungEntity)
                .where(hashtagEntity.hashtagStr.containsIgnoreCase(keyword), isSearchable())
                .groupBy(bungEntity.bungId)
                .orderBy(bungEntity.startDateTime.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
            case MEMBER -> queryFactory
                .select(bungEntity.bungId)
                .from(userBungEntity)
                .join(userBungEntity.bungEntity, bungEntity)
                .join(userBungEntity.userEntity, userEntity)
                .where(userEntity.nickname.containsIgnoreCase(keyword), isSearchable())
                .groupBy(bungEntity.bungId)
                .orderBy(bungEntity.startDateTime.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        };
    }

    private String normalizeKeyword(BungSearchCategory category, String keyword) {
        String normalizedKeyword = keyword.trim();
        if (BungSearchCategory.HASHTAG.equals(category)) {
            return normalizedKeyword.replaceFirst("^#+", "");
        }

        return normalizedKeyword;
    }

    private BooleanExpression isSearchable() {
        return bungEntity.startDateTime.gt(LocalDateTime.now())
            .and(bungEntity.faded.isFalse());
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
    @Transactional
    public void setAsFaded(List<Bung> bungs) {
        if(bungs.isEmpty()) {
            return;
        }

        List<String> bungIds = bungs.stream()
            .map(Bung::getBungId)
            .toList();

        queryFactory
            .update(bungEntity)
            .set(bungEntity.faded, true)
            .where(bungEntity.bungId.in(bungIds))
            .execute();
    }

    @Override
    public Bung save(Bung bung, List<BungHashtag> bungHashtags) {
        return Bung.from(bungJpaRepository.save(bung.toEntity(bungHashtags)));
    }

    @Override
    @Transactional
    public void updateBung(Bung bung) {
        queryFactory
            .update(bungEntity)
            .set(bungEntity.name, bung.getName())
            .set(bungEntity.description, bung.getDescription())
            .set(bungEntity.mainImage, bung.getMainImage())
            .set(bungEntity.memberNumber, bung.getMemberNumber())
            .set(bungEntity.hasAfterRun, bung.getHasAfterRun())
            .set(bungEntity.afterRunDescription, bung.getAfterRunDescription())
            .where(bungEntity.bungId.eq(bung.getBungId()))
            .execute();
    }

    @Override
    @Transactional
    public void setAsCompleted(String bungId) {
        queryFactory
            .update(bungEntity)
            .set(bungEntity.completed, true)
            .where(bungEntity.bungId.eq(bungId))
            .execute();
    }

    @Override
    public Page<Bung> findBungsPastStartWithSingleParticipant(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        BooleanExpression dateCondition = bungEntity.startDateTime.loe(now);
        BooleanExpression notFaded = bungEntity.faded.isFalse();
        BooleanExpression notCompleted = bungEntity.completed.isFalse();

        List<Bung> contents = queryFactory
            .select(bungEntity)
            .from(bungEntity)
            .leftJoin(userBungEntity).on(userBungEntity.bungEntity.eq(bungEntity))
            .where(dateCondition, notFaded, notCompleted)
            .groupBy(bungEntity.bungId)
            .having(userBungEntity.count().loe(1L))
            .orderBy(bungEntity.startDateTime.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch()
            .stream().map(Bung::from).toList();

        JPAQuery<Long> countQuery = queryFactory
            .select(bungEntity.countDistinct())
            .from(bungEntity)
            .leftJoin(userBungEntity).on(userBungEntity.bungEntity.eq(bungEntity))
            .where(dateCondition, notFaded, notCompleted)
            .groupBy(bungEntity.bungId)
            .having(userBungEntity.count().loe(1L));

        return PageableExecutionUtils.getPage(contents, pageable, () -> {
            List<Long> counts = countQuery.fetch();
            return counts != null ? counts.size() : 0L;
        });
    }

    @Override
    public Boolean isBungStarted(String bungId) {
        return queryFactory
            .selectOne()
            .from(bungEntity)
            .where(
                bungEntity.bungId.eq(bungId),
                bungEntity.startDateTime.loe(LocalDateTime.now())
            )
            .fetchFirst() != null;
    }

    private Set<String> getJoinedBungIds(User user) {
        return new HashSet<>(
            queryFactory
                .select(userBungEntity.bungEntity.bungId)
                .from(userBungEntity)
                .where(userBungEntity.userEntity.eq(user.toEntity()))
                .fetch()
        );
    }

    private BooleanExpression isAvailable(Set<String> joinedBungIds) {
        BooleanExpression baseCondition = bungEntity
            .startDateTime.gt(LocalDateTime.now())
            .and(bungEntity.faded.isFalse());

        if (joinedBungIds.isEmpty()) {
            return baseCondition;
        }

        return baseCondition.and(bungEntity.bungId.notIn(joinedBungIds));
    }

    private List<BungInfoWithMemberListDto> fetchBungInfoWithMembers(List<String> bungIds) {
        // 정렬 순서 보존을 위한 인덱스 맵 생성
        Map<String, Integer> orderMap = IntStream.range(0, bungIds.size())
            .boxed()
            .collect(Collectors.toMap(bungIds::get, Function.identity()));

        // 배치 조회
        List<Tuple> results = queryFactory
            .select(bungEntity, userBungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity).fetchJoin()
            .join(userBungEntity.userEntity, userEntity).fetchJoin()
            .where(bungEntity.bungId.in(bungIds))
            .fetch();

        // 그룹핑 및 DTO 변환
        Map<BungEntity, List<UserBungEntity>> bungMap = results.stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(bungEntity),
                Collectors.mapping(tuple -> tuple.get(userBungEntity), Collectors.toList())
            ));

        // 정렬된 결과 반환
        return bungMap.entrySet().stream()
            .map(entry -> new BungInfoWithMemberListDto(
                entry.getKey(),
                entry.getValue(),
                userProfileImageUrlResolver
            ))
            .sorted(Comparator.comparing(dto -> orderMap.get(dto.getBungId())))
            .collect(Collectors.toList());
    }
}
