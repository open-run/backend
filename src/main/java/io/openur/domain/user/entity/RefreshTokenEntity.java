package io.openur.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "tb_refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_refresh_token_blockchain_address", columnList = "blockchain_address")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "refresh_token_id")
    private String refreshTokenId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "blockchain_address", nullable = false, length = 42)
    private String blockchainAddress;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean revoked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    private RefreshTokenEntity(
        String tokenHash,
        String blockchainAddress,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
    ) {
        this.tokenHash = tokenHash;
        this.blockchainAddress = blockchainAddress;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.createdAt = createdAt;
    }

    public static RefreshTokenEntity issue(
        String tokenHash,
        String blockchainAddress,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
    ) {
        return new RefreshTokenEntity(tokenHash, blockchainAddress, expiresAt, createdAt);
    }

    public boolean isUsable(LocalDateTime now) {
        return !Boolean.TRUE.equals(revoked) && expiresAt.isAfter(now);
    }

    public void revoke(LocalDateTime now) {
        this.revoked = true;
        this.revokedAt = now;
    }
}
