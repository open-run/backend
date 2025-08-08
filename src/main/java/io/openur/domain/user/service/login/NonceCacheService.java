package io.openur.domain.user.service.login;

import io.openur.domain.user.model.EVMAddress;
import io.openur.domain.user.model.NonceData;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j(topic = "Nonce Cache Service")
@Component
class NonceCacheService {
    private Map<EVMAddress, NonceData> nonceMap = new HashMap<>();
    
    @Value("${nonce.cache.ttl-seconds:60}")
    private long ttlSeconds;

    public String getNonce(EVMAddress walletAddress) {
        cleanupExpiredEntries();
        log.debug("Getting nonce from cache for wallet: {}", walletAddress.getValue());
        NonceData data = nonceMap.get(walletAddress);
        if (data == null) {
            return null;
        }
        return data.getNonce();
    }

    public void evictNonce(EVMAddress walletAddress) {
        cleanupExpiredEntries();
        log.debug("Evicting nonce from cache for wallet: {}", walletAddress.getValue());
        nonceMap.remove(walletAddress);
    }

    public String generateAndStoreNonce(EVMAddress walletAddress) {
        cleanupExpiredEntries();
        String nonce = generateRandomNonce();
        log.debug("Storing nonce in cache for wallet: {}", walletAddress.getValue());
        nonceMap.put(walletAddress, new NonceData(nonce, ttlSeconds));
        return nonce;
    }

    private String generateRandomNonce() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64
            .getUrlEncoder()   // URL-safe variant avoids special characters
            .withoutPadding()
            .encodeToString(bytes);
    }

    private void cleanupExpiredEntries() {
        nonceMap.entrySet().removeIf(entry -> 
            entry.getValue().isExpired());
    }
} 