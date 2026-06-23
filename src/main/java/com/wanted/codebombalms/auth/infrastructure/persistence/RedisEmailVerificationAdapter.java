package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisEmailVerificationAdapter implements EmailVerificationRepository {

    private final StringRedisTemplate redisTemplate;

    // Redis 키 prefix
    private static final String KEY_CODE         = "email:verify:";
    private static final String KEY_VERIFIED     = "email:verified:";
    private static final String KEY_RECENT_SENT  = "email:sent:";
    private static final String KEY_SEND_COUNT   = "email:count:";

    // TTL
    private static final Duration TTL_CODE         = Duration.ofMinutes(3);
    private static final Duration TTL_VERIFIED     = Duration.ofMinutes(30);
    private static final Duration TTL_RECENT_SENT  = Duration.ofMinutes(1);
    private static final Duration TTL_SEND_COUNT   = Duration.ofMinutes(10);

    // 인증 코드

    @Override
    public void saveCode(String email, String code) {
        redisTemplate.opsForValue().set(KEY_CODE + email, code, TTL_CODE);
    }

    @Override
    public Optional<String> findCode(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_CODE + email));
    }

    @Override
    public void deleteCode(String email) {
        redisTemplate.delete(KEY_CODE + email);
    }

    // 인증 완료 플래그

    @Override
    public void markVerified(String email) {
        redisTemplate.opsForValue().set(KEY_VERIFIED + email, "true", TTL_VERIFIED);
    }

    @Override
    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_VERIFIED + email));
    }

    @Override
    public void clearVerified(String email) {
        redisTemplate.delete(KEY_VERIFIED + email);
    }

    // 재발송 쿨다운

    @Override
    public void markRecentlySent(String email) {
        redisTemplate.opsForValue().set(KEY_RECENT_SENT + email, "1", TTL_RECENT_SENT);
    }

    @Override
    public boolean isRecentlySent(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_RECENT_SENT + email));
    }

    // 발송 횟수 제한

    @Override
    public long incrementSendCount(String email) {
        String key = KEY_SEND_COUNT + email;
        Long count = redisTemplate.opsForValue().increment(key);

        // 첫 호출인 경우 TTL 설정
        if (count != null && count == 1L) {
            redisTemplate.expire(key, TTL_SEND_COUNT);
        }
        return count == null ? 0L : count;
    }
}
