package io.openur.global.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.openur.domain.challenge.model.ChallengeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtils {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper MAPPER = new ObjectMapper()
        .registerModule(
            new JavaTimeModule()
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        );
    
    public void set(String key, Object value) {
        if (value == null) {
            return;
        }
        
        String jsonValue;
        try {
            jsonValue = MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        
        redisTemplate.opsForValue().set(key, jsonValue);
    }
    
    public void set(String key, Object value, long ttl) {
        if (value == null) {
            return;
        }
        
        String jsonValue;
        try {
            jsonValue = MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        
        redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);
    }
    
    public <T> T get(String key, Class<T> clazz) {
        if(!redisTemplate.hasKey(key))
            throw new RuntimeException("Key not found: " + key);
        
        String jsonValue = (String) redisTemplate.opsForValue().get(key);
        
        try {
            return MAPPER.readValue(jsonValue, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public void removeByChallengeType(ChallengeType challengeType) {
        
    }
}
