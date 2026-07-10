package com.wanted.codebombalms.serviceevent.infrastructure.cleanup;

import com.wanted.codebombalms.global.application.cleanup.DefaultHardDeleteTarget;
import com.wanted.codebombalms.global.application.cleanup.port.HardDeleteTarget;
import com.wanted.codebombalms.serviceevent.application.port.ServiceEventStore;
import java.time.LocalDateTime;
import java.time.Period;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * service_event 2개월 보존 파기 (#605).
 * 빈만 등록하면 기존 HardDeleteScheduler(매일 03시)가 자동 실행한다 — 신규 스케줄러 불필요.
 * HardDeleteExecutor는 하루 1회만 호출하므로, 여기서 청크(1000행)를 소진될 때까지 반복한다.
 */
@Slf4j
@Configuration
public class ServiceEventCleanupConfig {

    private static final int CHUNK_SIZE = 1000;
    private static final int MAX_CHUNKS_PER_RUN = 200; // 1회 실행 상한 20만 행 — 03시 잡 폭주 방지

    @Bean
    public HardDeleteTarget serviceEventHardDeleteTarget(ServiceEventStore store) {
        return new DefaultHardDeleteTarget(
                "service-event",
                Period.ofMonths(2),
                threshold -> deleteUntilExhausted(store, threshold)
        );
    }

    private int deleteUntilExhausted(ServiceEventStore store, LocalDateTime threshold) {
        int total = 0;
        for (int chunk = 0; chunk < MAX_CHUNKS_PER_RUN; chunk++) {
            int deleted = store.deleteChunkCreatedBefore(threshold);
            total += deleted;
            if (deleted < CHUNK_SIZE) {
                return total;
            }
        }
        log.warn("event=service_event_cleanup_capped totalDeleted={} maxChunks={} — 잔여분은 다음 실행에서 처리",
                total, MAX_CHUNKS_PER_RUN);
        return total;
    }
}
