package com.wanted.codebombalms.serviceevent.application.query;

import com.wanted.codebombalms.serviceevent.domain.model.BriefingContent;
import java.time.LocalDateTime;

/**
 * GET /api/v1/admin/security/briefing 응답 data 형태 (#609).
 * stale=true 는 최신 생성이 실패해 직전 성공본을 보여주는 중이라는 뜻 (FE: 실패 뱃지).
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
