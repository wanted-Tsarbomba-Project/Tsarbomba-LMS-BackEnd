package com.wanted.codebombalms.serviceevent.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.global.domain.common.error.exception.TooManyRequestsException;
import com.wanted.codebombalms.serviceevent.application.port.ActiveLoginUserCountPort;
import com.wanted.codebombalms.serviceevent.application.port.BriefingLlmPort;
import com.wanted.codebombalms.serviceevent.application.port.SecuritySummaryQueryPort;
import com.wanted.codebombalms.serviceevent.application.query.BriefingResult;
import com.wanted.codebombalms.serviceevent.domain.exception.ServiceEventErrorCode;
import com.wanted.codebombalms.serviceevent.domain.model.BriefingContent;
import com.wanted.codebombalms.serviceevent.infrastructure.persistence.OpsBriefingJpaEntity;
import com.wanted.codebombalms.serviceevent.infrastructure.persistence.SpringDataOpsBriefingRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 브리핑 생성·조회 서비스.
 * 생성 = 최근 24시간 집계 → 마스킹 → LLM → ops_briefing 저장, 실패 시 FAILED 행 기록 후 직전 SUCCESS 본 유지(stale).
 * 조회 = 저장본만 반환 (LLM 호출 없음).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BriefingService {

    private static final Duration BRIEFING_WINDOW = Duration.ofHours(24);
    private static final Duration REGENERATE_COOLDOWN = Duration.ofMinutes(1);
    private static final List<LocalTime> SCHEDULE_TIMES =
            List.of(LocalTime.of(9, 5), LocalTime.of(15, 0), LocalTime.of(21, 0));
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final SecuritySummaryQueryPort summaryQuery;
    private final ActiveLoginUserCountPort loginUserCountPort;
    private final BriefingLlmPort briefingLlm;
    private final SpringDataOpsBriefingRepository briefingRepository;
    private final ObjectMapper objectMapper;

    private final AtomicReference<LocalDateTime> lastManualGeneration = new AtomicReference<>();

    /** 스케줄러 진입점 — 실패 시 예외 미전파 (FAILED 기록으로 충분) */
    public void generateScheduled() {
        try {
            generateAndStore();
        } catch (Exception e) {
            log.warn("event=briefing_scheduled_generation_failed", e);
        }
    }

    /** 수동 재생성 — 쿨다운(분당 1회) 통과 시 동기 생성 후 새 브리핑 반환 (실패는 502 전파) */
    public BriefingResult regenerate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = lastManualGeneration.get();
        if (last != null && Duration.between(last, now).compareTo(REGENERATE_COOLDOWN) < 0) {
            throw new TooManyRequestsException(ServiceEventErrorCode.BRIEFING_REGENERATE_COOLDOWN);
        }
        // CAS 로 슬롯 선점 — 동시 요청은 한 건만 통과 (쿨다운 인메모리 — 단일 인스턴스 전제)
        if (!lastManualGeneration.compareAndSet(last, now)) {
            throw new TooManyRequestsException(ServiceEventErrorCode.BRIEFING_REGENERATE_COOLDOWN);
        }

        generateAndStore();
        // 내부 호출은 프록시 미경유(self-invocation) — 비어노테이션 메서드 직접 사용
        return readLatest().orElseThrow(
                () -> new ExternalServiceException(ServiceEventErrorCode.BRIEFING_GENERATION_FAILED));
    }

    /** 최신 저장본 조회 — 생성 이력이 전혀 없으면 empty (FE: "브리핑 준비 중") */
    @Transactional(readOnly = true)
    public Optional<BriefingResult> getLatest() {
        return readLatest();
    }

    /** 최신 저장본 조회 본문 — getLatest 의 트랜잭션 안에서 실행 */
    private Optional<BriefingResult> readLatest() {
        Optional<OpsBriefingJpaEntity> latestSuccess =
                briefingRepository.findTopByStatusOrderByGeneratedAtDesc(OpsBriefingJpaEntity.STATUS_SUCCESS);
        if (latestSuccess.isEmpty()) {
            return Optional.empty();
        }

        OpsBriefingJpaEntity entity = latestSuccess.get();
        boolean stale = briefingRepository.findTopByOrderByGeneratedAtDesc()
                .map(latest -> !latest.isSuccess())
                .orElse(false);

        return Optional.of(new BriefingResult(
                entity.getId(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getGeneratedAt(),
                nextScheduledAt(LocalDateTime.now()),
                stale,
                deserializeContent(entity.getContentJson())
        ));
    }

    private void generateAndStore() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minus(BRIEFING_WINDOW);
        try {
            String aggregatesText = buildAggregatesText(start, end);
            BriefingContent content = briefingLlm.generate(
                    new BriefingLlmPort.BriefingSource(start, end, aggregatesText));
            briefingRepository.save(OpsBriefingJpaEntity.success(
                    start, end, briefingLlm.modelName(), serializeContent(content)));
            log.info("event=briefing_generated periodStart={} periodEnd={}", start, end);
        } catch (Exception e) {
            briefingRepository.save(OpsBriefingJpaEntity.failed(start, end, briefingLlm.modelName()));
            throw e;
        }
    }

    /** LLM 입력용 집계 텍스트 — PII 미반출 (집계 수치·마스킹 IP 라벨만, user_id 는 개수만) */
    private String buildAggregatesText(LocalDateTime start, LocalDateTime end) {
        LocalDateTime prevStart = start.minus(BRIEFING_WINDOW);

        var categories = summaryQuery.countByCategory(start, end);
        var prevCategories = summaryQuery.countByCategory(prevStart, start);
        var types = summaryQuery.countByType(start, end);
        var riskIps = summaryQuery.findTopRiskIps(start, end);
        var anomalies = summaryQuery.findHttpAnomalies(start, end);
        var hourly = summaryQuery.hourlyDistribution(start, end);
        var peak = summaryQuery.findConcurrentPeak(start, end);
        long loginUsers = loginUserCountPort.countDistinctLoginUsers(start, end);

        StringBuilder text = new StringBuilder();

        text.append("[카테고리별 이벤트 수 (직전 24시간 대비)]\n");
        for (var c : categories) {
            long prev = prevCategories.stream()
                    .filter(p -> p.category().equals(c.category()))
                    .mapToLong(SecuritySummaryQueryPort.CategoryCount::count)
                    .findFirst().orElse(0L);
            text.append("- %s: %d건 (직전 %d건)%n".formatted(c.category(), c.count(), prev));
        }

        text.append("\n[세부 타입 상위]\n");
        types.stream()
                .sorted(Comparator.comparingLong(SecuritySummaryQueryPort.TypeCount::count).reversed())
                .limit(8)
                .forEach(t -> text.append("- %s: %d건%n".formatted(t.type(), t.count())));

        text.append("\n[보안 이벤트 다발 IP (마스킹)]\n");
        if (riskIps.isEmpty()) {
            text.append("- 없음\n");
        }
        for (var ip : riskIps) {
            // 표적 계정 목록은 LIMIT 5 샘플 — 5개면 실제로는 그 이상 가능
            int targetCount = ip.targetUserIds().size();
            String targetLabel = targetCount >= 5 ? "5개 이상" : targetCount + "개";
            text.append("- %s: %d건, 주요타입 %s, 표적 계정 %s%n".formatted(
                    maskIp(ip.ip()), ip.count(), ip.mainType(), targetLabel));
        }

        text.append("\n[HTTP 예외 신호]\n");
        if (anomalies.isEmpty()) {
            text.append("- 없음\n");
        }
        for (var a : anomalies) {
            text.append("- %s %s: %d건%s%n".formatted(
                    a.route(), a.type(), a.count(),
                    a.maxDurationMs() == null ? "" : " (최대 %dms)".formatted(a.maxDurationMs())));
        }

        text.append("\n[시간대별 이벤트 집중 상위]\n");
        hourly.stream()
                .sorted(Comparator.comparingLong(SecuritySummaryQueryPort.HourlyCount::count).reversed())
                .limit(3)
                .forEach(h -> text.append("- %02d시: %d건%n".formatted(h.hour(), h.count())));

        text.append("\n[운영 지표]\n");
        text.append("- 로그인 고유 회원: %d명%n".formatted(loginUsers));
        peak.ifPresentOrElse(
                p -> text.append("- 최대 동시접속: %d명 (%s)%n".formatted(
                        p.peak(), p.occurredAt().format(HOUR_FORMAT))),
                () -> text.append("- 최대 동시접속: 집계 없음\n"));

        return text.toString();
    }

    /** IPv4 는 마지막 옥텟, IPv6 는 두 번째 그룹 이후를 마스킹 — 원본 IP 미반출 */
    static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "unknown";
        }
        if (ip.contains(".")) {
            int lastDot = ip.lastIndexOf('.');
            return ip.substring(0, lastDot) + ".xx";
        }
        String[] groups = ip.split(":");
        String prefix = groups.length >= 2 ? groups[0] + ":" + groups[1] : ip;
        return prefix + ":xx";
    }

    private LocalDateTime nextScheduledAt(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        return SCHEDULE_TIMES.stream()
                .map(today::atTime)
                .filter(t -> t.isAfter(now))
                .findFirst()
                .orElseGet(() -> today.plusDays(1).atTime(SCHEDULE_TIMES.get(0)));
    }

    private String serializeContent(BriefingContent content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            throw new ExternalServiceException(ServiceEventErrorCode.BRIEFING_GENERATION_FAILED, e);
        }
    }

    private BriefingContent deserializeContent(String json) {
        try {
            return objectMapper.readValue(json, BriefingContent.class);
        } catch (Exception e) {
            log.warn("event=briefing_content_deserialize_failed", e);
            return null; // 역직렬화 실패에도 조회 유지 — content null 이면 FE 가 재생성 유도
        }
    }
}
