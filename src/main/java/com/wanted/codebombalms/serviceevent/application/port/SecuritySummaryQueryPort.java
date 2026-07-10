package com.wanted.codebombalms.serviceevent.application.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * summary 집계 조회 포트 (#608). 서비스는 이 포트만 알고, 구현(native 쿼리)은 infra 에 둔다.
 * QueryDSL 도입(D트랙) 시 어댑터만 교체하면 된다.
 */
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

    long countDistinctActiveUsers(LocalDateTime start, LocalDateTime end);
}
