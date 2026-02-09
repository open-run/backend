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
import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.enums.GetBungResultEnum;
import io.openur.domain.bung.exception.GetBungException;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;

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

        // 카운트 쿼리 (최적화된 조건 사용)
        long totalCount = queryFactory
            .select(bungEntity.countDistinct())
            .from(bungEntity)
            .where(isAvailable(joinedBungIds))
            .fetchOne();

        return new PageImpl<>(contents, pageable, totalCount);
    }

    @Override
    public Page<BungInfoDto> findBungsWithLocation(String keyword, Pageable pageable) {
        // 1. 입력 검증
        if (!StringUtils.hasText(keyword)) {
            return Page.empty(pageable);
        }

        // 2. 공통 조건 추출
        BooleanExpression locationCondition = bungEntity.location.containsIgnoreCase(keyword);
        BooleanExpression dateCondition = bungEntity.startDateTime.gt(LocalDateTime.now());

        // 3. Content 쿼리 - 필요시 fetchJoin 추가 가능
        List<BungEntity> bungs = queryFactory
            .selectFrom(bungEntity)
            .where(locationCondition, dateCondition)
            .orderBy(bungEntity.startDateTime.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 4. DTO 변환
        List<BungInfoDto> contents = bungs.stream()
            .map(Bung::from)
            .map(BungInfoDto::new)
            .toList();

        // 5. Count 쿼리 최적화 (동일한 조건 사용)
        JPAQuery<Long> countQuery = queryFactory
            .select(bungEntity.count())
            .from(bungEntity)
            .where(locationCondition, dateCondition);

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<BungInfoDto> findBungWithHashtag(String hashTag, Pageable pageable) {
        // 공통 조건
        BooleanExpression hashtagCondition = hashtagEntity.hashtagStr.equalsIgnoreCase(hashTag);
        BooleanExpression dateCondition = bungEntity.startDateTime.gt(LocalDateTime.now());

        // 1. 먼저 정확한 총 개수 조회 (DISTINCT 기준)
        long total = queryFactory
            .select(bungHashtagEntity.bungEntity.bungId.countDistinct())
            .from(bungHashtagEntity)
            .join(bungHashtagEntity.hashtagEntity, hashtagEntity)
            .join(bungHashtagEntity.bungEntity, bungEntity)
            .where(hashtagCondition, dateCondition)
            .fetchOne();

        if (total == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 2. GROUP BY를 사용하여 중복 제거 후 페이징 적용 (성능 최적화)
        List<BungEntity> distinctBungs = queryFactory
            .select(bungEntity)
            .from(bungHashtagEntity)
            .join(bungHashtagEntity.hashtagEntity, hashtagEntity)
            .join(bungHashtagEntity.bungEntity, bungEntity)
            .where(hashtagCondition, dateCondition)
            .groupBy(bungEntity.bungId)           // ← GROUP BY로 중복 제거 (DISTINCT보다 효율적)
            .orderBy(bungEntity.startDateTime.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (distinctBungs.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        }

        List<String> bungIds = distinctBungs.stream()
            .map(BungEntity::getBungId)
            .toList();

        // 3. 상세 데이터 조회 (정렬 순서 보장)
        List<BungEntity> bungs = queryFactory
            .selectFrom(bungEntity)
            .where(bungEntity.bungId.in(bungIds))
            .orderBy(bungEntity.startDateTime.asc())  // 동일한 정렬 조건 적용
            .fetch();

        // 4. DTO 변환
        List<BungInfoDto> contents = bungs.stream()
            .map(Bung::from)
            .map(BungInfoDto::new)
            .toList();

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
    @Transactional
    public void setAsFaded(List<Bung> bungs) {
        if(bungs.isEmpty()) {
            return;
        }

        for (Bung bung : bungs) {
            bung.fadeBung();
            BungEntity entity = bung.toEntity(Collections.emptyList());

            entityManager.merge(entity);
        }

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public Bung save(Bung bung, List<BungHashtag> bungHashtags) {
        return Bung.from(bungJpaRepository.save(bung.toEntity(bungHashtags)));
    }

    @Override
    public Page<Bung> findBungsPastStartWithSingleParticipant(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();

        BooleanExpression dateCondition = bungEntity.startDateTime.loe(now);
        BooleanExpression notFaded = bungEntity.faded.isFalse();

        List<Bung> contents = queryFactory
            .select(bungEntity)
            .from(bungEntity)
            .leftJoin(userBungEntity).on(userBungEntity.bungEntity.eq(bungEntity))
            .where(dateCondition, notFaded)
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
            .where(dateCondition, notFaded)
            .groupBy(bungEntity.bungId)
            .having(userBungEntity.count().loe(1L));

        return PageableExecutionUtils.getPage(contents, pageable, () -> {
            List<Long> counts = countQuery.fetch();
            return counts != null ? counts.size() : 0L;
        });
    }

    @Override
    public Boolean isBungStarted(String bungId) {
        Bung bung = this.findBungById(bungId);
        return bung.getStartDateTime().isBefore(LocalDateTime.now());
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
        BooleanExpression baseCondition = bungEntity.startDateTime.gt(LocalDateTime.now());

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
            .map(entry -> new BungInfoWithMemberListDto(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(dto -> orderMap.get(dto.getBungId())))
            .collect(Collectors.toList());
    }
}
