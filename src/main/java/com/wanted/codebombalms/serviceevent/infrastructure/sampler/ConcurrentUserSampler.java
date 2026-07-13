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
 * 최대 동시접속자 샘플러 — 세션 키(auth:session:*) 개수를 주기 스냅샷해 ops_metric 으로 적재.
 * SCAN 사용 — KEYS 금지(운영 Redis 블로킹).
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
