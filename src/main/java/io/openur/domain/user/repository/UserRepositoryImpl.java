package io.openur.domain.user.repository;

import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User findUser(User user) {
        // Try to find by email first if available
        if (StringUtils.hasText(user.getEmail())) {
            User foundUser = findByEmail(user.getEmail());
            if (foundUser != null) {
                return foundUser;
            }
        }
        
        // If email not found or not available, try blockchain address
        if (user.getBlockchainAddress() != null && !user.getBlockchainAddress().matches("0x")) {
            return findByBlockchainAddress(user.getBlockchainAddress());
        }
        
        return null;
    }

    private User findByEmail(String email) {
        UserEntity userEntity = userJpaRepository.findByEmail(email).orElse(null);
        if (userEntity == null) {
            return null;
        } else {
            return User.from(userEntity);
        }
    }

    private User findByBlockchainAddress(String blockchainAddress) {
        UserEntity userEntity = userJpaRepository.findByBlockchainAddress(blockchainAddress).orElse(null);
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
    public List<String> batchIncrementFeedback(List<String> targetUserIds) {
        List<String> notFoundUserIds = new ArrayList<>(); // 조회되지 않은 userId를 저장할 리스트

        for (String userId : targetUserIds) {
            UserEntity userEntity = entityManager.find(UserEntity.class, userId);
            if (userEntity != null) {
                userEntity.setFeedback(userEntity.getFeedback() + 1);
                entityManager.merge(userEntity);
            } else {
                notFoundUserIds.add(userId); // 조회되지 않은 userId를 리스트에 추가
            }
        }
        entityManager.flush();
        entityManager.clear();

        return notFoundUserIds;
    }
}
