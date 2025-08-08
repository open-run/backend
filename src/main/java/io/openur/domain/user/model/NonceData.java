package io.openur.domain.user.model;

import java.time.Instant;

public record NonceData(String nonce, Instant timestamp, long ttlSeconds) {
    
    public NonceData(String nonce, long ttlSeconds) {
        this(nonce, Instant.now(), ttlSeconds);
    }

    public String getNonce() {
        return nonce;
    }

    public boolean isExpired() {
        return Instant.now().getEpochSecond() - timestamp.getEpochSecond() > ttlSeconds;
    }
}
