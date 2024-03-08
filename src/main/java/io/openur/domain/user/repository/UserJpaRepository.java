package io.openur.domain.user.repository;

import io.openur.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity,Long> {
    UserEntity findByEmail(@Param("email") String email);

    Optional<UserEntity> findById(Long userId);
}
