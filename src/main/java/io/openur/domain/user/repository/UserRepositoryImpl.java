package io.openur.domain.user.repository;

import static io.openur.domain.user.entity.QUserEntity.userEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.User;
import io.openur.global.common.validation.EthereumAddressValidator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final EthereumAddressValidator ethereumAddressValidator;
    private final JPAQueryFactory queryFactory;

    @Override
    public User findUser(User user) {
        if (!ethereumAddressValidator.isValid(user.getBlockchainAddress())) {
            return null;
        }
        
        UserEntity userEntity = userJpaRepository.findByBlockchainAddress(user.getBlockchainAddress()).orElse(null);
        if (userEntity == null) {
            return null;
        } else {
            return User.from(userEntity);
        }
    }

    @Override
    public User findById(String userId) {
        UserEntity userEntity = userJpaRepository.findByUserId(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found"));
        return User.from(userEntity);
    }

    @Override
    public List<User> findByUserNickname(String nickname) {
        List<UserEntity> userEntity = userJpaRepository.findAllByNicknameContains(nickname);
        return userEntity.stream().map(User::from).toList();
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }

    @Override
    @Transactional
    public User save(User user) {
        return User.from(userJpaRepository.save(user.toEntity()));
    }

    @Override
    public void update(User user) {
        userJpaRepository.save(user.toEntity());
    }

    @Override
    public void deleteUserInfo(User user) {
        userJpaRepository.delete(user.toEntity());
    }

    @Override
    @Transactional
    public List<String> batchIncrementFeedback(List<String> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return List.of();
        }

        // 1. 존재 확인 (1 SELECT) — N+1 find 제거
        Set<String> existingIds = new HashSet<>();
        userJpaRepository.findAllById(targetUserIds)
            .forEach(entity -> existingIds.add(entity.getUserId()));

        // 2. 존재하는 사용자 feedback 단일 bulk update (1 UPDATE)
        // 기존 dirty checking + merge 패턴은 영속 entity 에 대한 중복 호출이라 제거.
        // QueryDSL bulk update 는 즉시 DB 반영되며, 호출자 트랜잭션 컨텍스트를 건드리지 않음.
        if (!existingIds.isEmpty()) {
            queryFactory
                .update(userEntity)
                .set(userEntity.feedback, userEntity.feedback.add(1))
                .where(userEntity.userId.in(existingIds))
                .execute();
        }

        // 3. 미존재 userId 리스트 (입력 순서 보존)
        return targetUserIds.stream()
            .filter(id -> !existingIds.contains(id))
            .toList();
    }
}
