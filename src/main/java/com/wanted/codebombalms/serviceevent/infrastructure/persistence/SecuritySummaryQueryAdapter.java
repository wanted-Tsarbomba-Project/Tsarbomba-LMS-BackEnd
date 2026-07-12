package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import com.wanted.codebombalms.serviceevent.application.port.SecuritySummaryQueryPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecuritySummaryQueryAdapter implements SecuritySummaryQueryPort {

    private final SpringDataServiceEventRepository repository;

    @Override
    public List<CategoryCount> countByCategory(LocalDateTime start, LocalDateTime end) {
        return repository.countByCategory(start, end).stream()
                .map(row -> new CategoryCount(row.getCategory(), row.getCnt()))
                .toList();
    }

    @Override
    public List<TypeCount> countByType(LocalDateTime start, LocalDateTime end) {
        return repository.countByType(start, end).stream()
                .map(row -> new TypeCount(row.getEventType(), row.getCnt()))
                .toList();
    }

    @Override
    public List<RiskIp> findTopRiskIps(LocalDateTime start, LocalDateTime end) {
        // Top 10 고정이라 IP당 후속 조회 2회(대표 타입·표적 계정)는 최대 20쿼리 — 관리자 화면 허용 범위
        return repository.findTopSecurityIps(start, end).stream()
                .map(row -> new RiskIp(
                        row.getIpAddress(),
                        row.getCnt(),
                        repository.findMainSecurityTypeByIp(row.getIpAddress(), start, end),
                        repository.findTargetUserIdsByIp(row.getIpAddress(), start, end)))
                .toList();
    }

    @Override
    public List<RouteAnomaly> findHttpAnomalies(LocalDateTime start, LocalDateTime end) {
        return repository.findHttpAnomalies(start, end).stream()
                .map(row -> new RouteAnomaly(
                        row.getUri(), row.getEventType(), row.getCnt(), row.getMaxDuration()))
                .toList();
    }

    @Override
    public List<HourlyCount> hourlyDistribution(LocalDateTime start, LocalDateTime end) {
        return repository.hourlyDistribution(start, end).stream()
                .map(row -> new HourlyCount(row.getHr(), row.getCnt()))
                .toList();
    }

    @Override
    public Optional<ConcurrentPeak> findConcurrentPeak(LocalDateTime start, LocalDateTime end) {
        var row = repository.findConcurrentPeak(start, end);
        return row == null
                ? Optional.empty()
                : Optional.of(new ConcurrentPeak(row.getPeak(), row.getOccurredAt()));
    }

    @Override
    public long countDistinctActiveUsers(LocalDateTime start, LocalDateTime end) {
        return repository.countDistinctActiveUsers(start, end);
    }
}
