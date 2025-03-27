package io.openur.global.redis;

public interface RedisKeyProperties {
    String CHALLENGE_KEY = "challenge_{challengeId}";
    String CHALLENGE_KEY_PATTERN = "challenge_*";
    String CHALLENGE_ID = "challengeId";
}
