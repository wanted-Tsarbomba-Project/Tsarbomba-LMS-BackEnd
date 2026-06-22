package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.repository.OAuthStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisOAuthStateAdapter implements OAuthStateRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_STATE = "oauth:state:";   // + {state}
    private static final Duration TTL_STATE = Duration.ofMinutes(5);

    @Override
    public void save(String state) {
        redisTemplate.opsForValue().set(KEY_STATE + state, "1", TTL_STATE);
    }

    @Override
    public boolean validateAndDelete(String state) {
        return redisTemplate.opsForValue().getAndDelete(KEY_STATE + state) != null;
    }
}
