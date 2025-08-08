package io.openur.domain.user.service.login;

import io.openur.config.TestSupport;
import io.openur.domain.user.exception.InvalidSignatureException;
import io.openur.domain.user.model.EVMAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SmartWalletService Cache Tests")
@TestMethodOrder(OrderAnnotation.class)
class SmartWalletServiceCacheTest extends TestSupport {
    @Autowired
    private SmartWalletService smartWalletService;
    @Autowired
    private NonceCacheService nonceCacheService;
    private EVMAddress testWallet = new EVMAddress("0x1234567890123456789012345678901234567890");
    private EVMAddress wallet1 = new EVMAddress("0x1111111111111111111111111111111111111111");
    private EVMAddress wallet2 = new EVMAddress("0x2222222222222222222222222222222222222222");

    @Test
    @Order(1)
    @DisplayName("Should store nonce in cache when generating nonce and retrieve the same nonce from cache")
    void shouldStoreNonceInCacheAndRetrieveSameNonceFromCache() {
        String nonce = smartWalletService.getNonce(testWallet);
        assertThat(nonceCacheService.getNonce(testWallet)).isEqualTo(nonce);
    }

    @Test
    @Order(2)
    @DisplayName("Should evict nonce from cache after successful verification")
    void shouldEvictNonceAfterVerification() throws InvalidSignatureException {
        String nonce = smartWalletService.getNonce(testWallet);
        smartWalletService.verifyNonceSignature(testWallet, "signature", nonce);
        assertThat(nonceCacheService.getNonce(testWallet)).isNull();
    }

    @Test
    @Order(3)
    @DisplayName("Should throw exception when nonce is not found in cache")
    void shouldThrowExceptionWhenNonceNotFound() {
        assertThatThrownBy(() -> 
            smartWalletService.verifyNonceSignature(testWallet, "signature", "invalid-nonce")
        )
        .isInstanceOf(InvalidSignatureException.class)
        .hasMessageContaining("No stored nonce found for wallet");
    }

    @Test
    @Order(4)
    @DisplayName("Should throw exception when nonce mismatch")
    void shouldThrowExceptionWhenNonceMismatch() {
        smartWalletService.getNonce(testWallet);

        assertThatThrownBy(() -> 
            smartWalletService.verifyNonceSignature(testWallet, "signature", "wrong-nonce")
        )
        .isInstanceOf(InvalidSignatureException.class)
        .hasMessageContaining("Nonce mismatch for wallet");
    }

    @Test
    @Order(5)
    @DisplayName("Should generate different nonces for different wallet addresses")
    void shouldGenerateDifferentNoncesForDifferentWallets() {
        String nonce1 = smartWalletService.getNonce(wallet1);
        String nonce2 = smartWalletService.getNonce(wallet2);

        assertThat(nonceCacheService.getNonce(wallet1)).isEqualTo(nonce1);
        assertThat(nonceCacheService.getNonce(wallet2)).isEqualTo(nonce2);
        assertThat(nonce1).isNotEqualTo(nonce2);
    }

    @Test
    @Order(6)
    @DisplayName("Should handle multiple nonce generations and verifications")
    void shouldHandleMultipleNonceOperations() throws InvalidSignatureException {
        String nonce1 = smartWalletService.getNonce(wallet1);
        String nonce2 = smartWalletService.getNonce(wallet2);

        assertThat(nonceCacheService.getNonce(wallet1)).isEqualTo(nonce1);
        smartWalletService.verifyNonceSignature(wallet1, "signature1", nonce1);
        assertThat(nonceCacheService.getNonce(wallet1)).isNull();
        
        assertThat(nonceCacheService.getNonce(wallet2)).isEqualTo(nonce2);
        smartWalletService.verifyNonceSignature(wallet2, "signature2", nonce2);
        assertThat(nonceCacheService.getNonce(wallet2)).isNull();
    }

    @Test
    @Order(7)
    @DisplayName("Should generate new nonce after previous one is used")
    void shouldGenerateNewNonceAfterPreviousUsed() throws InvalidSignatureException {
        String firstNonce = smartWalletService.getNonce(testWallet);
        smartWalletService.verifyNonceSignature(testWallet, "signature", firstNonce);
        String secondNonce = smartWalletService.getNonce(testWallet);

        assertThat(secondNonce).isNotEqualTo(firstNonce);
        assertThat(nonceCacheService.getNonce(testWallet)).isEqualTo(secondNonce);
    }

    @Test
    @Order(8)
    @DisplayName("Should handle cache expiration")
    void shouldHandleCacheExpiration() throws InterruptedException {
        String nonce = smartWalletService.getNonce(testWallet);
        assertThat(nonceCacheService.getNonce(testWallet)).isEqualTo(nonce);
        Thread.sleep(3000); // Wait 3 seconds for 2-second TTL to expire
        assertThat(nonceCacheService.getNonce(testWallet)).isNull();
    }
} 