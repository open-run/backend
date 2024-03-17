package io.openur.domain.user.repository;


import io.openur.domain.user.entity.UserEntity;

import java.util.Optional;

public interface UserRepository {
    // QUESTION: What is the purpose of this interface repository?
    // Answer: 확장성 고려

    UserEntity save(UserEntity userEntity);

    UserEntity findByEmail(String email);

    Optional<UserEntity> findById(Long userId);

    boolean existsByNickname(String nickname);
}
