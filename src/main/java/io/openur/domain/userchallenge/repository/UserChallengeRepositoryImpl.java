package io.openur.domain.userchallenge.repository;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static io.openur.domain.challenge.entity.QChallengeEntity.challengeEntity;
import static io.openur.domain.challenge.entity.QChallengeStageEntity.challengeStageEntity;
import static io.openur.domain.userchallenge.entity.QUserChallengeEntity.userChallengeEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.challenge.enums.ChallengeType;
import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import io.openur.domain.userchallenge.model.UserChallenge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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
public class UserChallengeRepositoryImpl implements UserChallengeRepository {

    private final UserChallengeJpaRepository userChallengeJpaRepository;
    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserChallenge save(UserChallenge userChallenge) {
        return UserChallenge.from(
            userChallengeJpaRepository.save(userChallenge.toEntity()));
    }

    @Override
    @Transactional
    public void bulkIncrementCount(List<Long> userChallengeIds) {
        if (userChallengeIds.isEmpty()) {
            return;
        }

        queryFactory
            .update(userChallengeEntity)
            .set(userChallengeEntity.currentCount, userChallengeEntity.currentCount.add(1))
            .where(userChallengeEntity.userChallengeId.in(userChallengeIds))
            .execute();

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    @Transactional
    public void bulkUpdateCompletedChallenges(List<Long> userChallengeIds) {
        if (userChallengeIds.isEmpty()) {
            return;
        }

        queryFactory
            .update(userChallengeEntity)
            .set(userChallengeEntity.currentCount, userChallengeEntity.currentCount.add(1))
            .set(userChallengeEntity.completedDate, LocalDateTime.now())
            .set(userChallengeEntity.nftCompleted, false)
            .where(userChallengeEntity.userChallengeId.in(userChallengeIds))
            .execute();

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public Page<UserChallenge> findUncompletedChallengesByUserId(
        String userId,
        Pageable pageable
    ) {
        // 2. 공통 조건 추출 (가독성 및 오류 방지)
        BooleanExpression conditions = buildNormalUncompletedConditions(userId);

        // 3. Content 쿼리 (N+1 문제 방지)
        // 보상 가능한 챌린지(currentCount >= conditionCount)를 가장 위로 정렬
        List<UserChallengeEntity> content = queryFactory
            .selectFrom(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .fetchJoin()
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .fetchJoin()
            .where(conditions)
            .orderBy(
                // 보상 가능한 챌린지(0)를 먼저, 그 다음 일반 챌린지(1)를 정렬
                Expressions.cases()
                    .when(userChallengeEntity.currentCount.goe(challengeStageEntity.conditionAsCount))
                    .then(0)
                    .otherwise(1)
                    .asc(),
                // 각 그룹 내에서는 progress로 정렬
                userChallengeEntity.currentProgress.desc().nullsLast()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 4. 빈 결과 조기 반환
        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 5. Count 쿼리
        JPAQuery<Long> countQuery = queryFactory
            .select(userChallengeEntity.count())
            .from(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .where(conditions); // 조건 재사용

        // 6. 도메인 모델 매핑
        List<UserChallenge> result = content.stream()
            .map(UserChallenge::from)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<UserChallenge> findCompletedChallengesByUserId(
        String userId,
        Pageable pageable
    ) {
        // 2. 공통 조건 추출 (가독성 및 재사용성)
        BooleanExpression conditions = buildNftIssuableConditions(userId);

        // 3. Content 쿼리 (N+1 문제 해결)
        List<UserChallengeEntity> content = queryFactory
            .selectFrom(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .fetchJoin() // ✅ fetchJoin 추가
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .fetchJoin()         // ✅ fetchJoin 추가
            .where(conditions)
            .orderBy(
                userChallengeEntity.completedDate.asc(),
                userChallengeEntity.userChallengeId.asc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 4. 빈 결과 조기 반환
        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 5. Count 쿼리 (기존과 동일하게 효율적)
        JPAQuery<Long> countQuery = queryFactory
            .select(userChallengeEntity.count())
            .from(userChallengeEntity)
            .where(conditions); // ✅ 추출된 조건 재사용

        // 6. 도메인 모델 매핑
        List<UserChallenge> result = content.stream()
            .map(UserChallenge::from)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<UserChallenge> findRepetitiveChallengesByUserId(String userId,
        Pageable pageable) {

        // 1. 입력 검증
        if (!StringUtils.hasText(userId)) {
            return Page.empty(pageable);
        }

        // 2. 공통 조건 추출
        BooleanExpression conditions = buildRepetitiveUncompletedConditions(userId);

        // 3. Content 쿼리 (N+1 문제 방지)
        List<UserChallengeEntity> content = queryFactory
            .selectFrom(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .fetchJoin()
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .fetchJoin()
            .where(conditions)
            .orderBy(
                userChallengeEntity.currentProgress.desc().nullsLast(),
                userChallengeEntity.userChallengeId.asc() // 2차 정렬로 순서 보장
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 4. 빈 결과 조기 반환
        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 5. Count 쿼리 수정 (✅ Join 추가)
        JPAQuery<Long> countQuery = queryFactory
            .select(userChallengeEntity.count())
            .from(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity) // ✅ 조건절에서 참조하므로 필수
            .where(conditions);

        // 6. 도메인 모델 매핑
        List<UserChallenge> result = content.stream()
            .map(UserChallenge::from)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Map<Long, UserChallenge> findRepetitiveUserChallengesMappedByStageId(
        String userId,
        Long challengeId
    ) {
        // 2. fetchJoin으로 N+1 문제 해결
        List<UserChallengeEntity> entities = queryFactory
            .selectFrom(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .fetchJoin() // ✅ fetchJoin 추가
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .fetchJoin()         // ✅ fetchJoin 추가
            .where(
                userChallengeEntity.userEntity.userId.eq(userId),
                challengeEntity.challengeId.eq(challengeId) // ✅ challengeId 필터 추가
//                userChallengeEntity.nftCompleted.isFalse()
            )
            .fetch();

        // 3. 빈 결과 조기 반환
        if (entities.isEmpty()) {
            return Collections.emptyMap();
        }

        // 4. Map 변환
        return entities.stream()
            .map(UserChallenge::from)
            .collect(Collectors.toMap(
                userChallenge -> userChallenge.getChallengeStage().getStageId(),
                Function.identity()
            ));
    }

    @Override
    public Optional<UserChallenge> findFirstBySimpleRepetitiveChallenge(String userId
    ) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(userChallengeEntity)
                .join(userChallengeEntity.challengeStageEntity, challengeStageEntity)
                .join(challengeStageEntity.challengeEntity, challengeEntity)
                .where(
                    userChallengeEntity.userEntity.userId.eq(userId),
                    userChallengeEntity.completedDate.isNull(),
                    challengeEntity.conditionAsDate.isNull(),
                    challengeEntity.conditionAsText.isNull()
                )
                .orderBy(challengeStageEntity.stageNumber.asc())
                .fetchFirst()
        ).map(UserChallenge::from);
    }

    @Override
    public void delete(UserChallenge userChallenge) {
        userChallengeJpaRepository.delete(userChallenge.toEntity());
    }

    private BooleanExpression buildNormalUncompletedConditions(String userId) {
        return userChallengeEntity.userEntity.userId.eq(userId)
            .and(userChallengeEntity.completedDate.isNull()) // ✅ 미완료 조건 (isNull)
            .and(challengeEntity.challengeType.notIn(        // ✅ notIn으로 명확하게 처리
                ChallengeType.hidden,
                ChallengeType.repetitive
            ));
    }

    private BooleanExpression buildRepetitiveUncompletedConditions(String userId) {
        return userChallengeEntity.userEntity.userId.eq(userId)
            .and(userChallengeEntity.completedDate.isNull()) // ✅ 미완료 조건 (isNull)
            .and(challengeEntity.challengeType.eq(ChallengeType.repetitive));
    }

    /**
     * NFT 발급 가능한 챌린지 조회 조건 구성
     * 비즈니스 로직을 메서드로 분리하여 가독성 향상
     */
    private BooleanExpression buildNftIssuableConditions(String userId) {
        return userChallengeEntity.userEntity.userId.eq(userId)
            .and(userChallengeEntity.completedDate.isNotNull()) // 완료됨
            .and(userChallengeEntity.nftCompleted.isFalse());  // NFT 미발급
    }
}
