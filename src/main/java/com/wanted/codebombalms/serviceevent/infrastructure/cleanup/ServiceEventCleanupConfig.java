package com.wanted.codebombalms.serviceevent.infrastructure.cleanup;

import com.wanted.codebombalms.global.application.cleanup.DefaultHardDeleteTarget;
import com.wanted.codebombalms.global.application.cleanup.port.HardDeleteTarget;
import com.wanted.codebombalms.serviceevent.application.port.ServiceEventStore;
import com.wanted.codebombalms.serviceevent.infrastructure.persistence.SpringDataOpsBriefingRepository;
import java.time.LocalDateTime;
import java.time.Period;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * service_event 2개월 보존 파기 설정 — 기존 HardDeleteScheduler(매일 03시)가 실행.
 * 하루 1회 호출 — 청크(1000행) 소진까지 반복 삭제.
 */
@Slf4j
@Configuration
public class ServiceEventCleanupConfig {

    private static final int CHUNK_SIZE = 1000;
    private static final int MAX_CHUNKS_PER_RUN = 200; // 1회 실행 상한 20만 행 — 03시 잡 폭주 방지

    /** ops_briefing 2개월 동반 파기 */
    @Bean
    public HardDeleteTarget opsBriefingHardDeleteTarget(SpringDataOpsBriefingRepository repository) {
        return new DefaultHardDeleteTarget(
                "ops-briefing",
                Period.ofMonths(2),
                repository::deleteByGeneratedAtBefore
        );
    }

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
            int deleted;
            try {
                deleted = store.deleteChunkCreatedBefore(threshold);
            } catch (Exception e) {
                log.error("event=service_event_cleanup_failed totalDeleted={} — 남은 청크는 다음 실행(03시)에서 재시도",
                        total, e);
                return total; // 부분 성과 보존·예외 비전파 — 다른 HardDeleteTarget 파기 보호
            }
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
