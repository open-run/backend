package io.openur.global.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
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
    
    public boolean hasPatternKey(String pattern) {
        try (Cursor<String> cursor = redisTemplate.scan(
            ScanOptions.scanOptions().match(pattern).count(1).build()
        )) {
            return cursor.hasNext();
        }
    }
    
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }
    
    public void deleteByPattern(String pattern) {
        // 배치 크기 상수
        final int BATCH_SIZE = 100;
        
        try {
            // 패턴과 일치하는 키를 스캔하여 배치로 삭제
            ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(pattern)
                .count(BATCH_SIZE)
                .build();
            
            try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
                List<String> keysToDelete = new ArrayList<>(BATCH_SIZE);
                
                while (cursor.hasNext()) {
                    keysToDelete.add(cursor.next());
                    
                    // BATCH_SIZE 키마다 배치 삭제 실행
                    if (keysToDelete.size() >= BATCH_SIZE) {
                        batchDeleteKeys(keysToDelete);
                        keysToDelete.clear();
                    }
                }
                
                // 남은 키 삭제
                if (!keysToDelete.isEmpty()) {
                    batchDeleteKeys(keysToDelete);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete keys by pattern", e);
        }
    }
    
    private void batchDeleteKeys(List<String> keys) {
        if (keys.isEmpty()) {
            return;
        }
        
        try {
            redisTemplate.delete(keys);
        } catch (Exception e) {
            // 개별 삭제로 폴백
            for (String key : keys) {
                try {
                    redisTemplate.delete(key);
                } catch (Exception ex) {
                    log.warn("Failed to delete key: {}", key, ex);
                }
            }
        }
    }
}
