package com.wanted.codebombalms.serviceevent.application.query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /api/v1/admin/security/summary 응답 data 형태 (#608).
 * 필드 구조는 API.md M3 섹션·FE 전달 문서와 1:1 — 변경 시 두 문서 동시 갱신.
 */
public record SecuritySummaryResult(
        String period,
        Kpi kpi,
        List<DomainCount> domainCounts,
        List<HttpAnomaly> httpAnomalies,
        List<RiskIp> riskIps,
        List<Hourly> hourly
) {

    public record Kpi(
            long loginUsers,
            long maxConcurrent,
            LocalDateTime maxConcurrentAt,
            long totalEvents,
            Double totalEventsDeltaPct,
            long securityEvents,
            Double securityEventsDeltaPct,
            long http5xxCount,
            Double http5xxRatePct,
            long enrollments,
            Double enrollmentsDeltaPct
    ) {}

    public record DomainCount(String group, String label, long count) {}

    public record HttpAnomaly(String route, String type, long count, Integer maxDurationMs) {}

    public record RiskIp(String ip, String country, long eventCount, String mainType, List<Long> targetUserIds) {}

    public record Hourly(int hour, long count) {}
}
