package com.wanted.codebombalms.serviceevent.application.service;

import com.wanted.codebombalms.auth.domain.service.GeoIpResolver;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.serviceevent.application.port.ActiveLoginUserCountPort;
import com.wanted.codebombalms.serviceevent.application.port.SecuritySummaryQueryPort;
import com.wanted.codebombalms.serviceevent.application.query.SecuritySummaryResult;
import com.wanted.codebombalms.serviceevent.domain.exception.ServiceEventErrorCode;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventCategory;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 보안 요약 집계 조립 서비스.
 * 증감률(deltaPct)은 직전 동일 길이 구간과 비교 — today 는 어제 같은 시각까지, 2m 은 보존기간 밖이라 null.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecuritySummaryService {

    private static final Set<String> SECURITY_CATEGORY_CODES = Arrays.stream(ServiceEventCategory.values())
            .filter(ServiceEventCategory::isSecurity)
            .map(ServiceEventCategory::code)
            .collect(Collectors.toUnmodifiableSet());

    private static final String OPS_METRIC_CODE = ServiceEventCategory.OPS_METRIC.code();

    private static final Map<String, String> GROUP_LABELS = Map.of(
            "security", "보안",
            "enrollment", "수강",
            "learning", "학습",
            "content", "콘텐츠",
            "reward", "보상",
            "chatbot", "챗봇",
            "admin_audit", "관리",
            "http_anomaly", "HTTP"
    );

    private final SecuritySummaryQueryPort summaryQuery;
    private final ActiveLoginUserCountPort loginUserCountPort;
    private final GeoIpResolver geoIpResolver;

    public SecuritySummaryResult getSummary(String periodParam) {
        Period period = Period.from(periodParam);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = period.start(end);

        List<SecuritySummaryQueryPort.CategoryCount> categoryCounts = summaryQuery.countByCategory(start, end);
        Map<String, Long> typeCounts = toTypeCountMap(summaryQuery.countByType(start, end));

        long totalEvents = sumTotal(categoryCounts);
        long securityEvents = sumSecurity(categoryCounts);
        long enrollments = typeCounts.getOrDefault("enroll_created", 0L)
                - typeCounts.getOrDefault("enroll_cancelled", 0L);
        long http5xxCount = typeCounts.getOrDefault("http_5xx", 0L)
                + typeCounts.getOrDefault("http_502_external", 0L);

        Double totalDelta = null;
        Double securityDelta = null;
        Double enrollmentsDelta = null;
        if (period.comparable()) {
            LocalDateTime prevStart = period.previousStart(start, end);
            LocalDateTime prevEnd = period.previousEnd(start, end);
            List<SecuritySummaryQueryPort.CategoryCount> prevCategoryCounts =
                    summaryQuery.countByCategory(prevStart, prevEnd);
            Map<String, Long> prevTypeCounts = toTypeCountMap(summaryQuery.countByType(prevStart, prevEnd));

            totalDelta = deltaPct(totalEvents, sumTotal(prevCategoryCounts));
            securityDelta = deltaPct(securityEvents, sumSecurity(prevCategoryCounts));
            enrollmentsDelta = deltaPct(enrollments,
                    prevTypeCounts.getOrDefault("enroll_created", 0L)
                            - prevTypeCounts.getOrDefault("enroll_cancelled", 0L));
        }

        var concurrentPeak = summaryQuery.findConcurrentPeak(start, end);

        SecuritySummaryResult.Kpi kpi = new SecuritySummaryResult.Kpi(
                loginUserCountPort.countDistinctLoginUsers(start, end),
                concurrentPeak.map(SecuritySummaryQueryPort.ConcurrentPeak::peak).orElse(0L),
                concurrentPeak.map(SecuritySummaryQueryPort.ConcurrentPeak::occurredAt).orElse(null),
                totalEvents,
                totalDelta,
                securityEvents,
                securityDelta,
                http5xxCount,
                null, // 오류율 분모(전체 요청 수) 미수집 — v1 은 null
                enrollments,
                enrollmentsDelta
        );

        return new SecuritySummaryResult(
                period.code,
                kpi,
                toDomainCounts(categoryCounts),
                toHttpAnomalies(
                        summaryQuery.findHttpAnomalies(start, end),
                        summaryQuery.findHttpStatusBreakdown(start, end)),
                toRiskIps(summaryQuery.findTopRiskIps(start, end)),
                toHourly(summaryQuery.hourlyDistribution(start, end))
        );
    }

    private Map<String, Long> toTypeCountMap(List<SecuritySummaryQueryPort.TypeCount> counts) {
        return counts.stream().collect(Collectors.toMap(
                SecuritySummaryQueryPort.TypeCount::type,
                SecuritySummaryQueryPort.TypeCount::count));
    }

    /** ops_metric(동시접속 샘플)은 관측용 행 — 이벤트 수에서 제외 */
    private long sumTotal(List<SecuritySummaryQueryPort.CategoryCount> counts) {
        return counts.stream()
                .filter(c -> !OPS_METRIC_CODE.equals(c.category()))
                .mapToLong(SecuritySummaryQueryPort.CategoryCount::count)
                .sum();
    }

    private long sumSecurity(List<SecuritySummaryQueryPort.CategoryCount> counts) {
        return counts.stream()
                .filter(c -> SECURITY_CATEGORY_CODES.contains(c.category()))
                .mapToLong(SecuritySummaryQueryPort.CategoryCount::count)
                .sum();
    }

    private List<SecuritySummaryResult.DomainCount> toDomainCounts(
            List<SecuritySummaryQueryPort.CategoryCount> counts) {
        Map<String, Long> byGroup = counts.stream()
                .filter(c -> !OPS_METRIC_CODE.equals(c.category()))
                .collect(Collectors.groupingBy(
                        c -> SECURITY_CATEGORY_CODES.contains(c.category()) ? "security" : c.category(),
                        Collectors.summingLong(SecuritySummaryQueryPort.CategoryCount::count)));
        return byGroup.entrySet().stream()
                .map(e -> new SecuritySummaryResult.DomainCount(
                        e.getKey(), GROUP_LABELS.getOrDefault(e.getKey(), e.getKey()), e.getValue()))
                .sorted(Comparator.comparingLong(SecuritySummaryResult.DomainCount::count).reversed())
                .toList();
    }

    private List<SecuritySummaryResult.HttpAnomaly> toHttpAnomalies(
            List<SecuritySummaryQueryPort.RouteAnomaly> anomalies,
            List<SecuritySummaryQueryPort.StatusBreakdown> breakdowns) {
        Map<String, Map<Integer, Long>> byRouteType = breakdowns.stream()
                .collect(Collectors.groupingBy(
                        b -> routeTypeKey(b.route(), b.type()),
                        Collectors.toMap(
                                SecuritySummaryQueryPort.StatusBreakdown::status,
                                SecuritySummaryQueryPort.StatusBreakdown::count)));
        return anomalies.stream()
                .map(a -> new SecuritySummaryResult.HttpAnomaly(
                        a.route(), a.type(), a.count(), a.maxDurationMs(),
                        byRouteType.getOrDefault(routeTypeKey(a.route(), a.type()), Map.of())))
                .toList();
    }

    /** (route, type) 복합 키 — 널 안전, 구분자로 route 값 충돌 방지 */
    private String routeTypeKey(String route, String type) {
        return route + ' ' + type;
    }

    private List<SecuritySummaryResult.RiskIp> toRiskIps(List<SecuritySummaryQueryPort.RiskIp> riskIps) {
        return riskIps.stream()
                .map(ip -> new SecuritySummaryResult.RiskIp(
                        ip.ip(),
                        resolveCountrySafely(ip.ip()),
                        ip.count(),
                        ip.mainType(),
                        ip.targetUserIds()))
                .toList();
    }

    /** GeoIP 조회 실패(null/예외)를 IP 단위로 격리 — 요약 전체 실패 방지 */
    private String resolveCountrySafely(String ip) {
        try {
            return geoIpResolver.resolve(ip).country();
        } catch (Exception e) {
            log.warn("event=geoip_resolve_failed ip={} reason={}", ip, e.toString());
            return "Unknown";
        }
    }

    private List<SecuritySummaryResult.Hourly> toHourly(List<SecuritySummaryQueryPort.HourlyCount> hourly) {
        return hourly.stream()
                .map(h -> new SecuritySummaryResult.Hourly(h.hour(), h.count()))
                .toList();
    }

    /** 증감률(%) — 비교값 0이면 null (무한대 방지) */
    private Double deltaPct(long current, long previous) {
        if (previous == 0) {
            return null;
        }
        return Math.round((current - previous) * 1000.0 / previous) / 10.0;
    }

    private enum Period {
        TODAY("today") {
            @Override LocalDateTime start(LocalDateTime end) { return end.toLocalDate().atStartOfDay(); }
            @Override LocalDateTime previousStart(LocalDateTime start, LocalDateTime end) { return start.minusDays(1); }
            @Override LocalDateTime previousEnd(LocalDateTime start, LocalDateTime end) { return end.minusDays(1); } // 어제 "같은 시각"까지 — 부분일 공정 비교
            @Override boolean comparable() { return true; }
        },
        WEEK("week") {
            @Override LocalDateTime start(LocalDateTime end) { return end.minusDays(7); }
            @Override LocalDateTime previousStart(LocalDateTime start, LocalDateTime end) { return start.minusDays(7); }
            @Override LocalDateTime previousEnd(LocalDateTime start, LocalDateTime end) { return start; }
            @Override boolean comparable() { return true; }
        },
        TWO_MONTHS("2m") {
            @Override LocalDateTime start(LocalDateTime end) { return end.minusMonths(2); }
            @Override LocalDateTime previousStart(LocalDateTime start, LocalDateTime end) { return start; }
            @Override LocalDateTime previousEnd(LocalDateTime start, LocalDateTime end) { return start; }
            @Override boolean comparable() { return false; } // 비교 구간이 보존기간 밖
        };

        private final String code;

        Period(String code) {
            this.code = code;
        }

        abstract LocalDateTime start(LocalDateTime end);

        abstract LocalDateTime previousStart(LocalDateTime start, LocalDateTime end);

        abstract LocalDateTime previousEnd(LocalDateTime start, LocalDateTime end);

        abstract boolean comparable();

        static Period from(String param) {
            if (param == null || param.isBlank()) {
                return TODAY;
            }
            return Arrays.stream(values())
                    .filter(p -> p.code.equalsIgnoreCase(param))
                    .findFirst()
                    .orElseThrow(() -> new ValidationException(ServiceEventErrorCode.INVALID_SUMMARY_PERIOD));
        }
    }
}
