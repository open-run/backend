package io.openur.domain.user.service;

import io.openur.domain.user.dto.LoginNonceResponseDto;
import io.openur.domain.user.entity.LoginNonceEntity;
import io.openur.domain.user.repository.LoginNonceJpaRepository;
import io.openur.global.jwt.InvalidJwtException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginNonceService {

    private static final int NONCE_BYTE_LENGTH = 32;
    private static final String INVALID_LOGIN_PROOF_MESSAGE = "Invalid wallet login proof";

    private final LoginNonceJpaRepository loginNonceJpaRepository;
    private final SmartWalletSignatureVerifier signatureVerifier;
    private final SecureTokenGenerator secureTokenGenerator;
    private final LoginMessageFactory loginMessageFactory;

    @Value("${openrun.auth.login-nonce.max-age-seconds:300}")
    private long loginNonceMaxAgeSeconds;

    @Transactional
    public LoginNonceResponseDto issue(String blockchainAddress) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(loginNonceMaxAgeSeconds);
        String nonce = secureTokenGenerator.generateUrlSafeToken(NONCE_BYTE_LENGTH);
        String message = loginMessageFactory.create(blockchainAddress, nonce, expiresAt);

        loginNonceJpaRepository.save(LoginNonceEntity.issue(
            nonce,
            blockchainAddress,
            message,
            expiresAt,
            now
        ));

        return new LoginNonceResponseDto(nonce, message);
    }

    @Transactional
    public void consume(String blockchainAddress, String nonce, String signature) {
        LocalDateTime now = LocalDateTime.now();
        LoginNonceEntity loginNonceEntity = loginNonceJpaRepository.findByNonceForUpdate(nonce)
            .orElseThrow(() -> new InvalidJwtException(INVALID_LOGIN_PROOF_MESSAGE));

        if (!loginNonceEntity.isUsable(now)
            || !loginNonceEntity.getBlockchainAddress().equalsIgnoreCase(blockchainAddress)
            || !signatureVerifier.verify(blockchainAddress, loginNonceEntity.getMessage(), signature)) {
            throw new InvalidJwtException(INVALID_LOGIN_PROOF_MESSAGE);
        }

        loginNonceEntity.markUsed(now);
    }
}
