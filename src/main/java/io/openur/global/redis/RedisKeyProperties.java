package io.openur.global.redis;

public interface RedisKeyProperties {
    String CHALLENGE_ID = "challengeId";
    
    String CHALLENGE_KEY = "challenge_{challengeId}";
}
