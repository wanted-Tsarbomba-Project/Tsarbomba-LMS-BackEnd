package com.wanted.codebombalms.user.infrastructure.persistence;

import com.wanted.codebombalms.user.domain.repository.ProfileEditVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisProfileEditVerificationAdapter implements ProfileEditVerificationRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_VERIFIED = "profile:verified:"; // + {userId}
    private static final Duration TTL_VERIFIED = Duration.ofMinutes(3);

    @Override
    public void markVerified(Long userId) {
        redisTemplate.opsForValue().set(KEY_VERIFIED + userId, "1", TTL_VERIFIED);
    }

    @Override
    public boolean isVerified(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_VERIFIED + userId));
    }

    @Override
    public void clearVerified(Long userId) {
        redisTemplate.delete(KEY_VERIFIED + userId);
    }
}
