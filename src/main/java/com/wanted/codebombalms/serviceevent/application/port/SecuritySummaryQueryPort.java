package com.wanted.codebombalms.serviceevent.application.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** 보안 요약(summary) 집계 조회 포트. */
public interface SecuritySummaryQueryPort {

    record CategoryCount(String category, long count) {}
    record TypeCount(String type, long count) {}
    record RiskIp(String ip, long count, String mainType, List<Long> targetUserIds) {}
    record RouteAnomaly(String route, String type, long count, Integer maxDurationMs) {}
    record HourlyCount(int hour, long count) {}
    record ConcurrentPeak(long peak, LocalDateTime occurredAt) {}

    List<CategoryCount> countByCategory(LocalDateTime start, LocalDateTime end);

    List<TypeCount> countByType(LocalDateTime start, LocalDateTime end);

    /** 보안 이벤트 다발 IP Top 10 — 대표 타입·표적 계정(최대 5)까지 조립해 반환 */
    List<RiskIp> findTopRiskIps(LocalDateTime start, LocalDateTime end);

    List<RouteAnomaly> findHttpAnomalies(LocalDateTime start, LocalDateTime end);

    List<HourlyCount> hourlyDistribution(LocalDateTime start, LocalDateTime end);

    Optional<ConcurrentPeak> findConcurrentPeak(LocalDateTime start, LocalDateTime end);
}
