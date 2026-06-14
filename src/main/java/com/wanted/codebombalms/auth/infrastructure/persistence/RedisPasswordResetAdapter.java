package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.repository.PasswordResetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisPasswordResetAdapter implements PasswordResetRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_CODE        = "password:reset:";   // + {code} = email
    private static final String KEY_RECENT_SENT = "password:sent:";    // + {email}
    private static final String KEY_SEND_COUNT  = "password:count:";   // + {email}

    private static final Duration TTL_CODE        = Duration.ofMinutes(10);
    private static final Duration TTL_RECENT_SENT = Duration.ofMinutes(1);
    private static final Duration TTL_SEND_COUNT  = Duration.ofMinutes(10);

    @Override
    public boolean saveCodeIfAbsent(String email, String code) {
        Boolean saved = redisTemplate.opsForValue().setIfAbsent(KEY_CODE + code, email, TTL_CODE);
        return Boolean.TRUE.equals(saved);
    }

    @Override
    public Optional<String> findEmailByCode(String code) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_CODE + code));
    }

    @Override
    public Optional<String> findAndDeleteByCode(String code) {
        // GETDEL — 조회와 삭제를 단일 원자 연산으로 수행 (TOCTOU 제거, 단일 사용 보장)
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(KEY_CODE + code));
    }

    @Override
    public void markRecentlySent(String email) {
        redisTemplate.opsForValue().set(KEY_RECENT_SENT + email, "1", TTL_RECENT_SENT);
    }

    @Override
    public boolean isRecentlySent(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_RECENT_SENT + email));
    }

    @Override
    public long incrementSendCount(String email) {
        String key = KEY_SEND_COUNT + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, TTL_SEND_COUNT);
        }
        return count == null ? 0L : count;
    }
}
