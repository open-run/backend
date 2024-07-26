package io.openur.domain.bung.repository;

import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.user.entity.QUserEntity.userEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.dao.BungDAO;
import io.openur.domain.user.model.User;
import io.openur.global.enums.BungStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BungRepositoryImpl implements BungRepository, BungDAO {
    private final BungJpaRepository bungJpaRepository;


    @Override
    public Bung save(Bung bung) {
        return Bung.from(bungJpaRepository.save(bung.toEntity()));
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

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BungDetailDto> findBungs(User user, BungStatus status, Pageable pageable) {
        List<BungDetailDto> contents = queryFactory
            .selectDistinct(userBungEntity.bungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(
                statusFilter(user, status)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(bungEntity.startDateTime.desc())
            .fetch().stream().map(entity -> new BungDetailDto(Bung.from(entity))).toList();

        JPAQuery<Long> count = queryFactory
            .selectDistinct(userBungEntity.bungEntity.count())
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(
                statusFilter(user, status)
            );

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
    }

    private BooleanExpression statusFilter(User user, BungStatus status) {
        LocalDateTime currentTime = LocalDateTime.now();

        return switch (status) {
            // 끝난거
            case FINISHED -> bungEntity.startDateTime.gt(currentTime).and(bungEntity.endDateTime.gt(currentTime));
            //TODO: ** 수정 필요합니다. 참여가 가능한 벙, 시작 마감 이전이며, 참여 혹은 소유 하지 않은 벙 ( userBung 에 어떻게든 존재할 방법은 소유주 혹은 참가자 )
            case AVAILABLE -> bungEntity.startDateTime.lt(currentTime)
                .and(userBungEntity.isOwner.isFalse().or(userBungEntity.userEntity.ne(user.toEntity())));
            // 이미 참여한 벙 = 내가 주인이 아니면서, 참여 이력이 존재한 user bung entity
            case JOINED -> userEntity.eq(user.toEntity()).and(userBungEntity.isOwner.isFalse());
            // All 인 케이스, null 이더라도 포함이 되는 케이스
            default -> bungEntity.startDateTime.lt(currentTime);
        };
    }

    //TODO:필터 별로 수정될 정렬 방식 도입 추후 예정
//    private OrderSpecifier orderByStatus(BungStatus status) {
//
//    }
}
