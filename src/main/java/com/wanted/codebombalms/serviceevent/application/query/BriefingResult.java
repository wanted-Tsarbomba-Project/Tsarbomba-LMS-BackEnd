package com.wanted.codebombalms.serviceevent.application.query;

import com.wanted.codebombalms.serviceevent.domain.model.BriefingContent;
import java.time.LocalDateTime;

/**
 * GET /api/v1/admin/security/briefing 응답 data 형태.
 * stale=true — 최신 생성 실패로 직전 성공본 서비스 중.
 */
public record BriefingResult(
        Long briefingId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        LocalDateTime generatedAt,
        LocalDateTime nextScheduledAt,
        boolean stale,
        BriefingContent content
) {}
