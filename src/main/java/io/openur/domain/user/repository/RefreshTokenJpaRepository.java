package io.openur.domain.user.repository;

import io.openur.domain.user.entity.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RefreshTokenEntity r where r.tokenHash = :tokenHash")
    Optional<RefreshTokenEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update RefreshTokenEntity r
        set r.revoked = true, r.revokedAt = :revokedAt
        where lower(r.blockchainAddress) = lower(:blockchainAddress)
          and r.revoked = false
        """)
    int revokeActiveTokensByBlockchainAddress(
        @Param("blockchainAddress") String blockchainAddress,
        @Param("revokedAt") LocalDateTime revokedAt
    );
}
