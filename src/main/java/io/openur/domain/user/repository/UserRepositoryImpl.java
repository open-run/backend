package io.openur.domain.user.repository;

import io.openur.domain.user.entity.UserEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository{
    private final UserJpaRepository userJpaRepository;

    @Override
    public void save(UserEntity userEntity) {
        userJpaRepository.save(userEntity);
    }

    @Override
    public UserEntity findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }
    //Yejin Kelly Joo, findByEmail needs to be changed to Optional.

    @Override
    public Optional<UserEntity> findById(Long userId) {
        return userJpaRepository.findById(userId);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }
}
