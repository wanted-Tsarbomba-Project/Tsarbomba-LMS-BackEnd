package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.repository.AuthSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisAuthSessionAdapter implements AuthSessionRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_SESSION = "auth:session:"; // + {userId}

    @Override
    public void save(Long userId, String sid, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_SESSION + userId, sid, ttl);
    }

    @Override
    public Optional<String> findSid(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_SESSION + userId));
    }

    @Override
    public void delete(Long userId) {
        redisTemplate.delete(KEY_SESSION + userId);
    }
}
