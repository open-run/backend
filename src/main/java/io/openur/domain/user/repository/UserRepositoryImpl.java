package io.openur.domain.user.repository;

import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.User;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public UserEntity save(UserEntity userEntity) {
        return userJpaRepository.save(userEntity);
    }

    @Override
    public UserEntity findByEmail(String email) {
        return userJpaRepository.findByEmail(email).orElse(null);
    }

    @Override
    public UserEntity findById(Long userId) {
        return userJpaRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }

    @Override
    public void update(User user) {
        userJpaRepository.save(user.toEntity());
    }
}
