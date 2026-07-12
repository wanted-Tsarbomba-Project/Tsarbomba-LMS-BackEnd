package com.wanted.codebombalms.serviceevent.infrastructure.sampler;

import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventType;
import com.wanted.codebombalms.serviceevent.infrastructure.persistence.ServiceEventWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 최대 동시접속자 샘플러
 *
 * <p>P0 단일세션 Redis 키(auth:session:{userId})는 로그인 시 생기고
 * 로그아웃·잠금·만료 시 사라진다 — 즉 키 개수 = 현재 접속 중인 회원 수.
 * 5분마다 개수를 스냅샷해 ops_metric/concurrent_sample 로 적재하면
 * 기간 내 MAX(target_id) 가 최대 동시접속자가 된다 (±5분 오차).
 *
 * <p>반드시 SCAN 사용 — KEYS 는 운영 Redis 를 블로킹한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConcurrentUserSampler {

    private static final String SESSION_KEY_PATTERN = "auth:session:*";
    private static final int SCAN_BATCH = 500;

    private final StringRedisTemplate redisTemplate;
    private final ServiceEventWriter serviceEventWriter;

    @Scheduled(fixedDelayString = "${service-event.ops.concurrent-sample-delay-ms:300000}")
    public void sample() {
        try {
            long count = countSessionKeys();
            serviceEventWriter.write(ServiceEventEnvelope.opsMetric(
                    ServiceEventType.CONCURRENT_SAMPLE, count));
        } catch (Exception e) {
            log.warn("event=concurrent_sample_failed", e);
        }
    }

    private long countSessionKeys() {
        long count = 0;
        ScanOptions options = ScanOptions.scanOptions()
                .match(SESSION_KEY_PATTERN)
                .count(SCAN_BATCH)
                .build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                cursor.next();
                count++;
            }
        }
        return count;
    }
}
