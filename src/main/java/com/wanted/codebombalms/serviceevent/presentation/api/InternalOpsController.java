package com.wanted.codebombalms.serviceevent.presentation.api;

import com.wanted.codebombalms.serviceevent.application.port.OpsQueryPort;
import com.wanted.codebombalms.serviceevent.application.query.BriefingResult;
import com.wanted.codebombalms.serviceevent.application.service.BriefingService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FastAPI opschat 도구 전용 내부 조회 API — 외부 공개 아님.
 *
 * 인증: X-Internal-Token 헤더 = ${internal.api-token} (fail-closed — 설정이 비어 있으면 전부 401).
 * 파이썬의 DB 직접 조회를 대체해 쿼리·스키마 지식을 Spring 한 곳에 유지한다 (2026-07-20).
 * 응답은 opschat tools.py 가 LLM 도구 결과로 그대로 전달하는 계약 — ApiResponse 봉투 미사용.
 */
@Hidden
@RestController
@RequestMapping("/internal/ops")
@RequiredArgsConstructor
public class InternalOpsController {

    private final OpsQueryPort opsQueryPort;
    private final BriefingService briefingService;

    @Value("${internal.api-token:}")
    private String internalApiToken;

    @GetMapping("/events/count")
    public CountResponse countEvents(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String eventType
    ) {
        verify(token);
        List<OpsQueryPort.TypeCount> rows = opsQueryPort.countEvents(start, end, category, eventType);
        long total = rows.stream().mapToLong(OpsQueryPort.TypeCount::cnt).sum();
        return new CountResponse(start, end, total, rows);
    }

    @GetMapping("/events/timeline")
    public TimelineResponse eventTimeline(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String eventType
    ) {
        verify(token);
        return new TimelineResponse(start, end, "hour", opsQueryPort.eventTimeline(start, end, category, eventType));
    }

    @GetMapping("/events/top-ips")
    public TopIpsResponse topIps(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "10") int limit
    ) {
        verify(token);
        return new TopIpsResponse(start, end, opsQueryPort.topIps(start, end, category, eventType, limit));
    }

    @GetMapping("/events/recent")
    public RecentEventsResponse recentEvents(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "10") int limit
    ) {
        verify(token);
        return new RecentEventsResponse(start, end, opsQueryPort.recentEvents(start, end, category, eventType, limit));
    }

    @GetMapping("/briefing/latest")
    public BriefingResponse latestBriefing(
            @RequestHeader(value = "X-Internal-Token", required = false) String token
    ) {
        verify(token);
        return new BriefingResponse(briefingService.getLatest().orElse(null));
    }

    /** 상수시간 비교 + fail-closed. 실패 시 401 (내부 소비자 전용이라 봉투 없는 기본 에러로 충분) */
    private void verify(String token) {
        boolean configured = internalApiToken != null && !internalApiToken.isBlank();
        boolean matches = configured && token != null && MessageDigest.isEqual(
                internalApiToken.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8));
        if (!matches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 내부 토큰");
        }
    }

    public record CountResponse(LocalDateTime start, LocalDateTime end, long total,
                         List<OpsQueryPort.TypeCount> byEventType) {
    }

    public record TimelineResponse(LocalDateTime start, LocalDateTime end, String unit,
                            List<OpsQueryPort.TimelineBucket> timeline) {
    }

    public record TopIpsResponse(LocalDateTime start, LocalDateTime end,
                          List<OpsQueryPort.IpCount> topIps) {
    }

    public record RecentEventsResponse(LocalDateTime start, LocalDateTime end,
                                List<OpsQueryPort.EventDetail> events) {
    }

    public record BriefingResponse(BriefingResult briefing) {
    }
}
