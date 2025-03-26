package io.openur.global.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtils {
    private final RedisTemplate<String, Object> redisTemplate;
    
    protected static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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
        
        try {
            String jsonValue = MAPPER.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }
    
    public void set(String key, Object value, long ttl) {
        if (value == null) {
            return;
        }
        
        try {
            String jsonValue = MAPPER.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }
    
    public  <T> Optional<T> get(String key, Class<T> clazz) {
        if(!redisTemplate.hasKey(key)) return Optional.empty();
        Object value = redisTemplate.opsForValue().get(key);
        
        try {
            T result = MAPPER.readValue((String) value, clazz);
            return Optional.ofNullable(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize object", e);
        }
    }
    
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }
    
    public void batchDeleteKeys(List<String> keys) {
        try {
            Long deletedCount = redisTemplate.execute((RedisCallback<Long>) connection -> {
                byte[][] rawKeys = new byte[keys.size()][];
                
                // 키를 바이트 배열로 변환
                for (int i = 0; i < keys.size(); i++) {
                    rawKeys[i] = keys.get(i).getBytes(StandardCharsets.UTF_8);
                }
                
                // 다중 키 삭제 명령 사용
                if (rawKeys.length > 0) {
                    connection.del(rawKeys);
                    return (long) rawKeys.length;
                }
                
                return 0L;
            });
            
            log.debug("Batch deleted {} keys", deletedCount);
        } catch (Exception e) {
            log.error("Failed to batch delete keys, falling back to individual deletion", e);
            keys.forEach(this::deleteKey);
        }
    }
}
