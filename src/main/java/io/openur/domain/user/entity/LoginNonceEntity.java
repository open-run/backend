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
    name = "tb_login_nonces",
    indexes = {
        @Index(name = "idx_login_nonce_nonce", columnList = "nonce", unique = true),
        @Index(name = "idx_login_nonce_blockchain_address", columnList = "blockchain_address")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginNonceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "login_nonce_id")
    private String loginNonceId;

    @Column(nullable = false, unique = true, length = 96)
    private String nonce;

    @Column(name = "blockchain_address", nullable = false, length = 42)
    private String blockchainAddress;

    @Column(nullable = false, length = 512)
    private String message;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean used;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    private LoginNonceEntity(
        String nonce,
        String blockchainAddress,
        String message,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
    ) {
        this.nonce = nonce;
        this.blockchainAddress = blockchainAddress;
        this.message = message;
        this.expiresAt = expiresAt;
        this.used = false;
        this.createdAt = createdAt;
    }

    public static LoginNonceEntity issue(
        String nonce,
        String blockchainAddress,
        String message,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
    ) {
        return new LoginNonceEntity(nonce, blockchainAddress, message, expiresAt, createdAt);
    }

    public boolean isUsable(LocalDateTime now) {
        return !Boolean.TRUE.equals(used) && expiresAt.isAfter(now);
    }

    public void markUsed(LocalDateTime now) {
        this.used = true;
        this.usedAt = now;
    }
}
