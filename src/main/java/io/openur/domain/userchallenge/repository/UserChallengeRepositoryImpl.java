package io.openur.domain.userchallenge.repository;

import static io.openur.domain.challenge.entity.QChallengeEntity.challengeEntity;
import static io.openur.domain.challenge.entity.QChallengeStageEntity.challengeStageEntity;
import static io.openur.domain.userchallenge.entity.QUserChallengeEntity.userChallengeEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.challenge.entity.ChallengeStageEntity;
import io.openur.domain.challenge.entity.QChallengeStageEntity;
import io.openur.domain.challenge.enums.ChallengeType;
import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.userchallenge.dto.ChallengeRow;
import io.openur.domain.userchallenge.entity.QUserChallengeEntity;
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

    @Transactional
    @Override
    public void bulkInsertUserChallenges(List<UserChallenge> userChallenges) {
        if (userChallenges.isEmpty()) {
            return;
        }

        // saveAll 은 hibernate.jdbc.batch_size 설정 시 batch insert 로 동작.
        // 별도 persist 루프 + 수동 flush/clear 는 호출자 영속성 컨텍스트를 망가뜨려 제거.
        List<UserChallengeEntity> entities = userChallenges.stream()
            .map(UserChallenge::toEntity)
            .toList();
        userChallengeJpaRepository.saveAll(entities);
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

        // bulk update 는 영속성 컨텍스트를 우회하므로 1차 캐시가 stale.
        // flush(선행 dirty 보존) → clear(stale 정리) 로 후속 조회가 DB fresh read 하도록 보장.
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
    @Transactional
    public void markNftCompleted(Long userChallengeId) {
        queryFactory
            .update(userChallengeEntity)
            .set(userChallengeEntity.nftCompleted, true)
            .where(userChallengeEntity.userChallengeId.eq(userChallengeId))
            .execute();

        // bulk update 후 flush → clear 로 같은 트랜잭션의 stale UserChallenge 제거.
        // (NftMintJobProcessor.markSuccess 가 같은 트랜잭션에서 mintJob dirty 를 함께 flush)
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public boolean existsByUserId(String userId) {
        Integer one = queryFactory
            .selectOne()
            .from(userChallengeEntity)
            .where(userChallengeEntity.userEntity.userId.eq(userId))
            .fetchFirst();
        return one != null;
    }

    @Override
    public Page<ChallengeRow> findUncompletedChallengesByUserId(
        String userId,
        Pageable pageable
    ) {
        BooleanExpression stageIsCurrent = buildStageIsCurrentForUser(userId);
        BooleanExpression typeFilter = challengeStageEntity.challengeEntity.challengeType
            .notIn(ChallengeType.hidden, ChallengeType.repetitive);

        return findChallengeRowsByConditions(userId, pageable, stageIsCurrent, typeFilter);
    }

    @Override
    public Page<UserChallenge> findCompletedChallengesByUserId(
        String userId,
        Pageable pageable
    ) {
        BooleanExpression conditions = buildNftIssuableConditions(userId);

        List<UserChallengeEntity> content = queryFactory
            .selectFrom(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .fetchJoin()
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .fetchJoin()
            .where(conditions)
            .orderBy(
                userChallengeEntity.completedDate.asc(),
                userChallengeEntity.userChallengeId.asc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        JPAQuery<Long> countQuery = queryFactory
            .select(userChallengeEntity.count())
            .from(userChallengeEntity)
            .where(conditions);

        List<UserChallenge> result = content.stream()
            .map(UserChallenge::from)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<UserChallenge> findCompletedAndNftIssuedChallengesByUserId(
        String userId,
        Pageable pageable
    ) {
        BooleanExpression conditions = buildNftIssuedConditions(userId);

        List<UserChallengeEntity> content = queryFactory
            .selectFrom(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .fetchJoin()
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .fetchJoin()
            .where(conditions)
            .orderBy(
                userChallengeEntity.completedDate.desc(),
                userChallengeEntity.userChallengeId.desc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        JPAQuery<Long> countQuery = queryFactory
            .select(userChallengeEntity.count())
            .from(userChallengeEntity)
            .where(conditions);

        List<UserChallenge> result = content.stream()
            .map(UserChallenge::from)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ChallengeRow> findRepetitiveChallengesByUserId(
        String userId,
        Pageable pageable
    ) {
        if (!StringUtils.hasText(userId)) {
            return Page.empty(pageable);
        }

        BooleanExpression stageIsCurrent = buildStageIsCurrentForUser(userId);
        BooleanExpression typeFilter = challengeStageEntity.challengeEntity.challengeType
            .eq(ChallengeType.repetitive);

        return findChallengeRowsByConditions(userId, pageable, stageIsCurrent, typeFilter);
    }

    private Page<ChallengeRow> findChallengeRowsByConditions(
        String userId,
        Pageable pageable,
        BooleanExpression stageIsCurrent,
        BooleanExpression typeFilter
    ) {
        List<ChallengeStageEntity> stages = queryFactory
            .selectFrom(challengeStageEntity)
            .innerJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .fetchJoin()
            .leftJoin(userChallengeEntity)
            .on(
                userChallengeEntity.challengeStageEntity.stageId.eq(challengeStageEntity.stageId),
                userChallengeEntity.userEntity.userId.eq(userId)
            )
            .where(stageIsCurrent, typeFilter)
            .orderBy(
                Expressions.cases()
                    .when(userChallengeEntity.currentCount.coalesce(0)
                        .goe(challengeStageEntity.conditionAsCount))
                    .then(0).otherwise(1).asc(),
                userChallengeEntity.currentProgress.coalesce(0.0f).desc(),
                challengeStageEntity.challengeEntity.challengeId.asc(),
                challengeStageEntity.stageId.asc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (stages.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Map<Long, UserChallengeEntity> ucByStageId = fetchUserChallengeMap(userId, stages);

        JPAQuery<Long> countQuery = queryFactory
            .select(challengeStageEntity.countDistinct())
            .from(challengeStageEntity)
            .innerJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .where(stageIsCurrent, typeFilter);

        List<ChallengeRow> result = stages.stream()
            .map(stage -> new ChallengeRow(
                ChallengeStage.from(stage),
                Optional.ofNullable(ucByStageId.get(stage.getStageId()))
                    .map(UserChallenge::from)
                    .orElse(null)
            ))
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Map<Long, UserChallenge> findRepetitiveUserChallengesMappedByStageId(
        String userId,
        Long challengeId
    ) {
        List<UserChallengeEntity> entities = queryFactory
            .selectFrom(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .fetchJoin()
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .fetchJoin()
            .where(
                userChallengeEntity.userEntity.userId.eq(userId),
                challengeEntity.challengeId.eq(challengeId),
                userChallengeEntity.nftCompleted.isFalse()
            )
            .fetch();

        if (entities.isEmpty()) {
            return Collections.emptyMap();
        }

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
    public List<UserChallenge> findAllBySimpleRepetitiveChallenge(String userId) {
        return queryFactory
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
            .fetch()
            .stream()
            .map(UserChallenge::from)
            .toList();
    }

    @Override
    public void delete(UserChallenge userChallenge) {
        userChallengeJpaRepository.delete(userChallenge.toEntity());
    }

    /**
     * "표시할 stage" 결정 규칙:
     * (A) 보상 수령 가능한 완료 stage가 있으면 그 stage가 표시 대상이다.
     * (B) 보상 수령 가능한 stage가 없고, 미완료 user_challenge가 있으면 그 stage가 표시 대상이다.
     * (C) 해당 user가 이 challenge에 user_challenge를 하나도 가지지 않았고 (= 신규 유저),
     *     이 stage가 해당 challenge의 stage_number 최소값을 가진 stage이면 표시 대상이다.
     *
     * 결과적으로 challenge 1개당 최대 1개의 stage만 통과:
     * - 신규 유저: stage 1
     * - 진행 중 유저: 진행 중인 stage
     * - 달성 후 보상 미수령 유저: 보상 수령 가능한 stage
     * - 모든 stage가 NFT 보상까지 완료된 challenge: 통과 안 함
     */
    private BooleanExpression buildStageIsCurrentForUser(String userId) {
        QUserChallengeEntity ucSub = new QUserChallengeEntity("ucSub");
        QChallengeStageEntity stageSub = new QChallengeStageEntity("stageSub");
        QChallengeStageEntity stageMinSub = new QChallengeStageEntity("stageMinSub");
        QUserChallengeEntity rewardableUcSub = new QUserChallengeEntity("rewardableUcSub");
        QChallengeStageEntity rewardableStageSub = new QChallengeStageEntity("rewardableStageSub");
        QUserChallengeEntity incompleteUcSub = new QUserChallengeEntity("incompleteUcSub");
        QChallengeStageEntity incompleteStageSub = new QChallengeStageEntity("incompleteStageSub");

        BooleanExpression hasRewardableOnThisStage = JPAExpressions
            .selectOne()
            .from(ucSub)
            .where(
                ucSub.challengeStageEntity.stageId.eq(challengeStageEntity.stageId),
                ucSub.userEntity.userId.eq(userId),
                ucSub.completedDate.isNotNull(),
                ucSub.nftCompleted.isFalse()
            )
            .exists();

        BooleanExpression hasRewardableForThisChallenge = JPAExpressions
            .selectOne()
            .from(rewardableUcSub)
            .join(rewardableUcSub.challengeStageEntity, rewardableStageSub)
            .where(
                rewardableStageSub.challengeEntity.challengeId.eq(
                    challengeStageEntity.challengeEntity.challengeId),
                rewardableUcSub.userEntity.userId.eq(userId),
                rewardableUcSub.completedDate.isNotNull(),
                rewardableUcSub.nftCompleted.isFalse()
            )
            .exists();

        BooleanExpression isFirstRewardableStage = challengeStageEntity.stageNumber.eq(
            JPAExpressions
                .select(rewardableStageSub.stageNumber.min())
                .from(rewardableUcSub)
                .join(rewardableUcSub.challengeStageEntity, rewardableStageSub)
                .where(
                    rewardableStageSub.challengeEntity.challengeId.eq(
                        challengeStageEntity.challengeEntity.challengeId),
                    rewardableUcSub.userEntity.userId.eq(userId),
                    rewardableUcSub.completedDate.isNotNull(),
                    rewardableUcSub.nftCompleted.isFalse()
                )
        );

        BooleanExpression hasIncompleteOnThisStage = JPAExpressions
            .selectOne()
            .from(ucSub)
            .where(
                ucSub.challengeStageEntity.stageId.eq(challengeStageEntity.stageId),
                ucSub.userEntity.userId.eq(userId),
                ucSub.completedDate.isNull()
            )
            .exists();

        BooleanExpression hasIncompleteForThisChallenge = JPAExpressions
            .selectOne()
            .from(incompleteUcSub)
            .join(incompleteUcSub.challengeStageEntity, incompleteStageSub)
            .where(
                incompleteStageSub.challengeEntity.challengeId.eq(
                    challengeStageEntity.challengeEntity.challengeId),
                incompleteUcSub.userEntity.userId.eq(userId),
                incompleteUcSub.completedDate.isNull()
            )
            .exists();

        BooleanExpression isFirstIncompleteStage = challengeStageEntity.stageNumber.eq(
            JPAExpressions
                .select(incompleteStageSub.stageNumber.min())
                .from(incompleteUcSub)
                .join(incompleteUcSub.challengeStageEntity, incompleteStageSub)
                .where(
                    incompleteStageSub.challengeEntity.challengeId.eq(
                        challengeStageEntity.challengeEntity.challengeId),
                    incompleteUcSub.userEntity.userId.eq(userId),
                    incompleteUcSub.completedDate.isNull()
                )
        );

        BooleanExpression hasNoUcForThisChallenge = JPAExpressions
            .selectOne()
            .from(ucSub)
            .join(ucSub.challengeStageEntity, stageSub)
            .where(
                stageSub.challengeEntity.challengeId.eq(challengeStageEntity.challengeEntity.challengeId),
                ucSub.userEntity.userId.eq(userId)
            )
            .notExists();

        BooleanExpression isMinStage = challengeStageEntity.stageNumber.eq(
            JPAExpressions
                .select(stageMinSub.stageNumber.min())
                .from(stageMinSub)
                .where(stageMinSub.challengeEntity.challengeId.eq(challengeStageEntity.challengeEntity.challengeId))
        );

        return hasRewardableOnThisStage.and(isFirstRewardableStage)
            .or(
                hasRewardableForThisChallenge.not()
                    .and(hasIncompleteOnThisStage)
                    .and(isFirstIncompleteStage)
            )
            .or(
                hasRewardableForThisChallenge.not()
                    .and(hasIncompleteForThisChallenge.not())
                    .and(hasNoUcForThisChallenge)
                    .and(isMinStage)
            );
    }

    private Map<Long, UserChallengeEntity> fetchUserChallengeMap(
        String userId, List<ChallengeStageEntity> stages
    ) {
        List<Long> stageIds = stages.stream()
            .map(ChallengeStageEntity::getStageId)
            .toList();
        return queryFactory
            .selectFrom(userChallengeEntity)
            .where(
                userChallengeEntity.challengeStageEntity.stageId.in(stageIds),
                userChallengeEntity.userEntity.userId.eq(userId)
            )
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                uc -> uc.getChallengeStageEntity().getStageId(),
                Function.identity(),
                (a, b) -> a
            ));
    }

    private BooleanExpression buildNftIssuableConditions(String userId) {
        return userChallengeEntity.userEntity.userId.eq(userId)
            .and(userChallengeEntity.completedDate.isNotNull())
            .and(userChallengeEntity.nftCompleted.isFalse());
    }

    /**
     * NFT 발급 완료된 챌린지 조회 조건 구성
     * completedDate IS NOT NULL && nftCompleted = true
     */
    private BooleanExpression buildNftIssuedConditions(String userId) {
        return userChallengeEntity.userEntity.userId.eq(userId)
            .and(userChallengeEntity.completedDate.isNotNull()) // 완료됨
            .and(userChallengeEntity.nftCompleted.isTrue());   // NFT 발급 완료
    }
}
