package com.wanted.codebombalms.serviceevent.application.port;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 운영 Q&A 챗봇 도구용 읽기 전용 집계 포트.
 *
 * FastAPI opschat 의 function calling 도구가 내부 API(/internal/ops/*)를 통해 호출한다 —
 * 쿼리·스키마 지식을 Spring 한 곳에 유지하기 위해 파이썬의 DB 직접 조회를 대체 (2026-07-20).
 * category / eventType 필터는 null 허용(전체).
 */
public interface OpsQueryPort {

    List<TypeCount> countEvents(LocalDateTime start, LocalDateTime end, String category, String eventType);

    List<TimelineBucket> eventTimeline(LocalDateTime start, LocalDateTime end, String category, String eventType);

    List<IpCount> topIps(LocalDateTime start, LocalDateTime end, String category, String eventType, int limit);

    List<EventDetail> recentEvents(LocalDateTime start, LocalDateTime end, String category, String eventType, int limit);

    record TypeCount(String eventType, long cnt) {
    }

    /** bucket = "yyyy-MM-dd HH:00" (KST 벽시계, 시간 단위) */
    record TimelineBucket(String bucket, long cnt) {
    }

    record IpCount(String ipAddress, long cnt) {
    }

    /** detail 컬럼은 개인정보 여지가 있어 의도적으로 미포함 (opschat 방침과 동일) */
    record EventDetail(
            String category,
            String eventType,
            Long userId,
            String ipAddress,
            String uri,
            Integer httpStatus,
            Integer durationMs,
            String traceId,
            LocalDateTime createdAt
    ) {
    }
}
