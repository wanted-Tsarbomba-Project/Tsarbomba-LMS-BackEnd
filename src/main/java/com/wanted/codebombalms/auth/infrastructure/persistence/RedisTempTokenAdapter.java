package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.auth.domain.model.OAuthTempData;
import com.wanted.codebombalms.auth.domain.repository.TempTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisTempTokenAdapter implements TempTokenRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_TEMP = "oauth:temp:";   // + {tempToken}
    private static final Duration TTL_TEMP = Duration.ofMinutes(10);

    @Override
    public void save(String tempToken, OAuthTempData data) {
        // 저장 + TTL 을 단일 SET EX 로 원자 처리 (expire 분리 제거)
        redisTemplate.opsForValue().set(KEY_TEMP + tempToken, serialize(data), TTL_TEMP);
    }

    @Override
    public Optional<OAuthTempData> find(String tempToken) {
        return deserialize(redisTemplate.opsForValue().get(KEY_TEMP + tempToken));
    }

    @Override
    public Optional<OAuthTempData> findAndDelete(String tempToken) {
        // GETDEL — 조회+삭제 단일 원자 연산 (TOCTOU 제거, 단일 사용 보장)
        return deserialize(redisTemplate.opsForValue().getAndDelete(KEY_TEMP + tempToken));
    }

    private String serialize(OAuthTempData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("OAuthTempData 직렬화 실패", e);
        }
    }

    private Optional<OAuthTempData> deserialize(String json) {
        if (json == null) {
            return Optional.empty();   // 없거나 만료
        }
        try {
            return Optional.of(objectMapper.readValue(json, OAuthTempData.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("OAuthTempData 역직렬화 실패", e);
        }
    }
}
