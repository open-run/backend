package io.openur.domain.challenge.repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.challenge.entity.ChallengeStageEntity;
import io.openur.domain.challenge.entity.QChallengeStageEntity;
import io.openur.domain.challenge.model.ChallengeStage;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeStageRepositoryImpl implements ChallengeStageRepository {
    private final ChallengeStageJpaRepository challengeStageJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChallengeStage> findAllByChallengeId(Long challengeId) {
        return challengeStageJpaRepository
            .findAllByChallengeEntityChallengeIdOrderByStageNumberAsc(challengeId)
            .stream()
            .map(ChallengeStage::from)
            .toList();
    }

    @Override
    public Optional<ChallengeStage> findByChallengeIdAndStageIsGreaterThan(
        Long challengeId, Integer stage) {
        return challengeStageJpaRepository
            .findFirstByChallengeEntity_ChallengeIdAndStageNumberIsGreaterThan(
                challengeId, stage
            )
            .map(ChallengeStage::from);
    }

    @Override
    public Page<ChallengeStage> findAllByMinimumStages(Pageable pageable) {
        QChallengeStageEntity stage = QChallengeStageEntity.challengeStageEntity;
        QChallengeStageEntity stageSub = new QChallengeStageEntity("stageSub");

        // 1. 데이터 조회 (offset, limit 적용)
        List<ChallengeStageEntity> content = queryFactory
            .selectFrom(stage)
            .where(stage.stageNumber.eq(
                JPAExpressions
                    .select(stageSub.stageNumber.min())
                    .from(stageSub)
                    .where(stageSub.challengeEntity.eq(stage.challengeEntity))
            ))
            .offset(pageable.getOffset())  // ✅ 페이징 시작 위치
            .limit(pageable.getPageSize()) // ✅ 페이지 크기
            .fetch();

        // 2. 전체 개수 조회 (count 쿼리)
        Long total = queryFactory
            .select(stage.count())
            .from(stage)
            .where(stage.stageNumber.eq(
                JPAExpressions
                    .select(stageSub.stageNumber.min())
                    .from(stageSub)
                    .where(stageSub.challengeEntity.eq(stage.challengeEntity))
            ))
            .fetchOne();

        // 3. Domain 객체로 변환
        List<ChallengeStage> domainContent = content.stream()
            .map(ChallengeStage::from)
            .toList();

        // 4. Page 객체 생성
        return new PageImpl<>(domainContent, pageable, total != null ? total : 0L);
    }

}
