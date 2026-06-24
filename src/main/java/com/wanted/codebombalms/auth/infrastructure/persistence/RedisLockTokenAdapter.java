package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.repository.LockTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisLockTokenAdapter implements LockTokenRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_LOCK = "account:lock:"; // + {token}
    private static final Duration TTL_LOCK = Duration.ofHours(24);

    @Override
    public void save(String token, Long userId) {
        redisTemplate.opsForValue().set(KEY_LOCK + token, String.valueOf(userId), TTL_LOCK);
    }

    @Override
    public Optional<Long> findUserIdAndDelete(String token) {
        String value = redisTemplate.opsForValue().getAndDelete(KEY_LOCK + token);
        return value == null ? Optional.empty() : Optional.of(Long.valueOf(value));
    }
}
