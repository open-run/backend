package io.openur.domain.userbung.repository;

import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.user.entity.QUserEntity.userEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.dto.GetUsersResponseDto;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.dao.UserBungDAO;
import java.awt.HeadlessException;
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
    public UserBung save(UserBung userBung) {
        return UserBung.from(userBungJpaRepository.save(userBung.toEntity()));
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
    public List<Bung> findAllJoinBungsByUser(User user) {
        return queryFactory
            .selectDistinct(userBungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .where(
                userBungEntity.userEntity.eq(user.toEntity())
            ).fetch().stream().map(entity -> Bung.from(entity.getBungEntity())).toList();
    }

    @Override
    public Page<GetUsersResponseDto> findAllFrequentUsers(List<Bung> bungs,
        User currentUser, Pageable pageable) {
        List<BungEntity> bungEntities = bungs.stream().map(Bung::toEntity).toList();

        List<GetUsersResponseDto> contents = queryFactory
            .select(userBungEntity.userEntity, userBungEntity.countDistinct())
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .where(
                userBungEntity.userEntity.ne(currentUser.toEntity()),
                userBungEntity.bungEntity.in(bungEntities)
            )
            .groupBy(userEntity)
            .orderBy(userBungEntity.countDistinct().desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch().stream().map(GetUsersResponseDto::new).toList();

        JPAQuery<Long> count = queryFactory
            .select(userEntity.countDistinct())
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .where(
                userBungEntity.userEntity.ne(currentUser.toEntity()),
                userBungEntity.bungEntity.in(bungEntities)
            )
            .groupBy(userEntity);

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
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
    public void removeUserFromBung(UserBung userBung) {
        userBungJpaRepository.delete(userBung.toEntity());
    }
}
