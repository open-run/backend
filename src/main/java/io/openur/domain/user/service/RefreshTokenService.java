package io.openur.domain.user.service;

import io.openur.domain.user.entity.RefreshTokenEntity;
import io.openur.domain.user.repository.RefreshTokenJpaRepository;
import io.openur.domain.user.repository.UserJpaRepository;
import io.openur.global.jwt.InvalidJwtException;
import io.openur.global.jwt.JwtUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_BYTE_LENGTH = 64;
    private static final String INVALID_REFRESH_TOKEN_MESSAGE = "Invalid refresh token";

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final JwtUtil jwtUtil;
    private final SecureTokenGenerator secureTokenGenerator;
    private final TokenHashService tokenHashService;

    @Value("${openrun.auth.refresh-token.max-age-seconds:2592000}")
    private long refreshTokenMaxAgeSeconds;

    @Transactional
    public String issueRefreshToken(String blockchainAddress) {
        String refreshToken = secureTokenGenerator.generateUrlSafeToken(TOKEN_BYTE_LENGTH);
        saveRefreshToken(refreshToken, blockchainAddress, LocalDateTime.now());
        return refreshToken;
    }

    @Transactional
    public SessionTokens rotate(String refreshToken) {
        LocalDateTime now = LocalDateTime.now();
        RefreshTokenEntity refreshTokenEntity = findUsableRefreshToken(refreshToken, now);
        String blockchainAddress = refreshTokenEntity.getBlockchainAddress();

        if (userJpaRepository.findByBlockchainAddress(blockchainAddress).isEmpty()) {
            refreshTokenEntity.revoke(now);
            throw new InvalidJwtException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        refreshTokenEntity.revoke(now);

        String newRefreshToken = secureTokenGenerator.generateUrlSafeToken(TOKEN_BYTE_LENGTH);
        saveRefreshToken(newRefreshToken, blockchainAddress, now);

        return new SessionTokens(jwtUtil.createToken(blockchainAddress), newRefreshToken);
    }

    @Transactional
    public void revoke(String refreshToken) {
        String tokenHash = tokenHashService.sha256(refreshToken);
        LocalDateTime now = LocalDateTime.now();
        refreshTokenJpaRepository.findByTokenHashForUpdate(tokenHash)
            .ifPresent(refreshTokenEntity ->
                refreshTokenJpaRepository.revokeActiveTokensByBlockchainAddress(
                    refreshTokenEntity.getBlockchainAddress(),
                    now
                )
            );
    }

    private RefreshTokenEntity findUsableRefreshToken(String refreshToken, LocalDateTime now) {
        String tokenHash = tokenHashService.sha256(refreshToken);
        RefreshTokenEntity refreshTokenEntity = refreshTokenJpaRepository.findByTokenHashForUpdate(tokenHash)
            .orElseThrow(() -> new InvalidJwtException(INVALID_REFRESH_TOKEN_MESSAGE));

        if (!refreshTokenEntity.isUsable(now)) {
            throw new InvalidJwtException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        return refreshTokenEntity;
    }

    private void saveRefreshToken(String refreshToken, String blockchainAddress, LocalDateTime now) {
        refreshTokenJpaRepository.save(RefreshTokenEntity.issue(
            tokenHashService.sha256(refreshToken),
            blockchainAddress,
            now.plusSeconds(refreshTokenMaxAgeSeconds),
            now
        ));
    }

    public record SessionTokens(String jwtToken, String refreshToken) {
    }
}
