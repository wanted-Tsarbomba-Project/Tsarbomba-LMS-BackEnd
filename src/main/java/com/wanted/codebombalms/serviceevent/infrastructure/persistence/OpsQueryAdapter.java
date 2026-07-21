package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import com.wanted.codebombalms.serviceevent.application.port.OpsQueryPort;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpsQueryAdapter implements OpsQueryPort {

    // 리포지토리 네이티브 쿼리의 고정 LIMIT 과 동기 유지
    private static final int MAX_LIMIT = 20;

    private final SpringDataServiceEventRepository repository;

    @Override
    public List<TypeCount> countEvents(LocalDateTime start, LocalDateTime end, String category, String eventType) {
        return repository.countByTypeFiltered(start, end, category, eventType).stream()
                .map(row -> new TypeCount(row.getEventType(), row.getCnt()))
                .toList();
    }

    @Override
    public List<TimelineBucket> eventTimeline(LocalDateTime start, LocalDateTime end, String category, String eventType) {
        return repository.eventTimeline(start, end, category, eventType).stream()
                .map(row -> new TimelineBucket(row.getBucket(), row.getCnt()))
                .toList();
    }

    @Override
    public List<IpCount> topIps(LocalDateTime start, LocalDateTime end, String category, String eventType, int limit) {
        return repository.topIpsFiltered(start, end, category, eventType).stream()
                .limit(clamp(limit))
                .map(row -> new IpCount(row.getIpAddress(), row.getCnt()))
                .toList();
    }

    @Override
    public List<EventDetail> recentEvents(LocalDateTime start, LocalDateTime end, String category, String eventType, int limit) {
        return repository.recentEvents(start, end, category, eventType).stream()
                .limit(clamp(limit))
                .map(row -> new EventDetail(
                        row.getCategory(), row.getEventType(), row.getUserId(), row.getIpAddress(),
                        row.getUri(), row.getHttpStatus(), row.getDurationMs(), row.getTraceId(),
                        row.getCreatedAt()))
                .toList();
    }

    private long clamp(int limit) {
        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }
}
