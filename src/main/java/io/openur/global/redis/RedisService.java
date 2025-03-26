package io.openur.global.redis;

import static io.openur.global.redis.RedisKeyProperties.CHALLENGE_ID;
import static io.openur.global.redis.RedisKeyProperties.CHALLENGE_KEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service @Slf4j
public class RedisService extends RedisUtils {
    
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
        
        Resource challengeJson = new ClassPathResource("challenge.json");
        JsonNode challengeNode;
        
        try (InputStream inputStream = challengeJson.getInputStream()) {
            challengeNode = MAPPER.readTree(inputStream).get("challenges");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        
        challengeNode.forEach(node -> {
            String challengeId = node.get(CHALLENGE_ID).asText();
            
            try {
                set(
                    CHALLENGE_KEY.replace(CHALLENGE_ID, challengeId),
                    MAPPER.writeValueAsString(node)
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
