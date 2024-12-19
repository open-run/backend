package io.openur.domain.user.repository;

import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.User;
import java.util.List;
import java.util.NoSuchElementException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User findByEmail(String email) {
        UserEntity userEntity = userJpaRepository.findByEmail(email).orElse(null);
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
    public void batchIncrementFeedback(List<String> targetUserIds) {
        for (String userId : targetUserIds) {
            UserEntity userEntity = entityManager.find(UserEntity.class, userId);
            if (userEntity != null) {
                userEntity.setFeedback(userEntity.getFeedback() + 1);
                entityManager.merge(userEntity);
            }
        }
        entityManager.flush();
        entityManager.clear();
    }
}
