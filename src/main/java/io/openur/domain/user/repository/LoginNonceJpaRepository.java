package io.openur.domain.user.repository;

import io.openur.domain.user.entity.LoginNonceEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoginNonceJpaRepository extends JpaRepository<LoginNonceEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from LoginNonceEntity n where n.nonce = :nonce")
    Optional<LoginNonceEntity> findByNonceForUpdate(@Param("nonce") String nonce);
}
