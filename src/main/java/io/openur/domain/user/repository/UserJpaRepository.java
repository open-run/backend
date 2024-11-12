package io.openur.domain.user.repository;

import io.openur.domain.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUserId(String userId);

    List<UserEntity> findAllByNicknameContains(String nickname);

    boolean existsByNickname(String nickname);
}
