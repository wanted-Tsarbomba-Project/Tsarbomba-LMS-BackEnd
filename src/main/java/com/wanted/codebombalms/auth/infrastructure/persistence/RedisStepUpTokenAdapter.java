package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.auth.domain.model.StepUpChallenge;
import com.wanted.codebombalms.auth.domain.repository.StepUpTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisStepUpTokenAdapter implements StepUpTokenRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_CHALLENGE = "stepup:challenge:"; // + {token}
    private static final String KEY_ATTEMPTS  = "stepup:attempts:";  // + {token}
    private static final Duration TTL = Duration.ofMinutes(5);

    private static final String KEY_DEVICE = "stepup:device:"; // + {userId}:{deviceFp}

    @Override
    public void save(String token, StepUpChallenge challenge) {
        // 본문 + TTL 단일 SET EX (원자)
        redisTemplate.opsForValue().set(KEY_CHALLENGE + token, serialize(challenge), TTL);
    }

    @Override
    public Optional<StepUpChallenge> find(String token) {
        return deserialize(redisTemplate.opsForValue().get(KEY_CHALLENGE + token));
    }

    @Override
    public void delete(String token) {
        // 챌린지에 담긴 userId+deviceFp 로 기기 예약 키(KEY_DEVICE)도 함께 정리 — 생명주기 동기화
        find(token).ifPresent(challenge ->
                redisTemplate.delete(KEY_DEVICE + challenge.userId() + ":" + challenge.deviceFp()));
        redisTemplate.delete(KEY_CHALLENGE + token);
        redisTemplate.delete(KEY_ATTEMPTS + token);
    }

    @Override
    public int incrementAttempts(String token) {
        String key = KEY_ATTEMPTS + token;
        Long count = redisTemplate.opsForValue().increment(key); // 원자 INCR
        if (count != null && count == 1L) {
            redisTemplate.expire(key, TTL); // 첫 시도 시 TTL 부여 (토큰과 동기 만료)
        }
        return count == null ? 0 : count.intValue();
    }

    @Override
    public Optional<String> reserveDeviceChallenge(Long userId, String deviceFp, String newToken) {
        String key = KEY_DEVICE + userId + ":" + deviceFp;

        // 원자적 SETNX — 키가 없을 때만 newToken 으로 선점 (TTL 5분)
        Boolean reserved = redisTemplate.opsForValue().setIfAbsent(key, newToken, TTL);
        if (Boolean.TRUE.equals(reserved)) {
            return Optional.empty(); // 선점 성공 → 신규 발급 진행
        }

        // 이미 발급 진행 중/완료 → 기존 토큰 재사용
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    private String serialize(StepUpChallenge challenge) {
        try {
            return objectMapper.writeValueAsString(challenge);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("StepUpChallenge 직렬화 실패", e);
        }
    }

    private Optional<StepUpChallenge> deserialize(String json) {
        if (json == null) {
            return Optional.empty(); // 없거나 만료
        }
        try {
            return Optional.of(objectMapper.readValue(json, StepUpChallenge.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("StepUpChallenge 역직렬화 실패", e);
        }
    }
}
