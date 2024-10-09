package io.openur.domain.userbung.repository;

import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
import java.awt.HeadlessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserBungRepositoryImpl implements UserBungRepository, UserBungDAO {
    private final UserBungJpaRepository userBungJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BungDetailDto> findBungsWithStatus(User user, BungStatus status,
        Pageable pageable) {
        List<BungDetailDto> contents = queryFactory
            .selectDistinct(userBungEntity.bungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(withStatus(user, status))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch().stream().map(entity -> new BungDetailDto(Bung.from(entity)))
            .toList();

        JPAQuery<Long> count = queryFactory
            .selectDistinct(userBungEntity.bungEntity.count())
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(withStatus(user, status));

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
    }

    @Override
    public UserBung findByUserIdAndBungId(String userId, String bungId) {
        UserBungEntity userBungEntity = userBungJpaRepository
            .findByUserEntity_UserIdAndBungEntity_BungId(userId, bungId)
            .orElseThrow(() ->
                new NoSuchElementException(
                    String.format(
                        "UserBung not found by given userId(%s) and bungId(%s)",
                        userId, bungId
                    )
                )
            );
        return UserBung.from(userBungEntity);
    }

    @Override
    public UserBung findCurrentOwner(String bungId) {
        List<UserBungEntity> userBungEntities = userBungJpaRepository.findByBungEntity_BungId(
            bungId);
        if (userBungEntities.isEmpty()) {
            throw new NoSuchElementException(
                String.format("UserBung not found by given bungId(%s)", bungId)
            );
        }

        return UserBung.from(userBungEntities.stream()
            .filter(UserBungEntity::isOwner)
            .findFirst()
            .orElseThrow(() ->
                new HeadlessException(
                    String.format("Owner not found by given bungId(%s)", bungId)
                )
            )
        );
    }

    @Override
    public UserBung save(UserBung userBung) {
        return UserBung.from(userBungJpaRepository.save(userBung.toEntity()));
    }

    @Override
    public void removeUserFromBung(UserBung userBung) {
        userBungJpaRepository.delete(userBung.toEntity());
    }

    private BooleanExpression withStatus(User user, BungStatus status) {
        BooleanExpression conditions = userBungEntity.userEntity.eq(user.toEntity());

        if(BungStatus.PENDING.equals(status))
            // 참여는 한 것이 조건, 시작 예정이라면 시작 시간이 지금보다는 이후일것.
            return conditions.and(bungEntity.startDateTime.gt(LocalDateTime.now()));
        // 종료된 상황이라면 종료 시간이 지금보다 이전일것.
        return conditions.and(bungEntity.endDateTime.loe(LocalDateTime.now()));
    }
}
