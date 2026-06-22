package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.model.OAuthTempData;
import com.wanted.codebombalms.auth.domain.repository.TempTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisTempTokenAdapter implements TempTokenRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_TEMP = "oauth:temp:";   // + {tempToken}
    private static final Duration TTL_TEMP = Duration.ofMinutes(10);
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_NAME  = "name";

    @Override
    public void save(String tempToken, OAuthTempData data) {
        String key = KEY_TEMP + tempToken;
        redisTemplate.opsForHash().putAll(key, Map.of(
                FIELD_EMAIL, data.email(),
                FIELD_NAME, data.name() == null ? "" : data.name()
        ));
        redisTemplate.expire(key, TTL_TEMP);
    }


        @Override
    public Optional<OAuthTempData> find(String tempToken) {
        String key = KEY_TEMP + tempToken;
        Object email = redisTemplate.opsForHash().get(key, FIELD_EMAIL);
        if (email == null) {
            return Optional.empty();   // 없거나 만료
        }
        Object name = redisTemplate.opsForHash().get(key, FIELD_NAME);
        return Optional.of(new OAuthTempData((String) email, (String) name));
        // 삭제하지 않음 — 표시용 조회
    }

    @Override
    public Optional<OAuthTempData> findAndDelete(String tempToken) {
        String key = KEY_TEMP + tempToken;
        Object email = redisTemplate.opsForHash().get(key, FIELD_EMAIL);
        if (email == null) {
            return Optional.empty();   // 없거나 만료
        }
        Object name = redisTemplate.opsForHash().get(key, FIELD_NAME);
        redisTemplate.delete(key);     // 단일 사용 — 조회 후 즉시 삭제
        return Optional.of(new OAuthTempData((String) email, (String) name));
    }
}
