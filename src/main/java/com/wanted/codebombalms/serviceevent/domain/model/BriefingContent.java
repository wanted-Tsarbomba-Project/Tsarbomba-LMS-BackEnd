package com.wanted.codebombalms.serviceevent.domain.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * AI 브리핑 본문.
 * LLM 구조화 출력 스키마·ops_briefing.content_json·브리핑 응답 content 필드가 이 record 하나로 정합.
 */
public record BriefingContent(
        @JsonPropertyDescription("전체 상황을 한 문장으로 요약한 헤드라인 (가장 중요한 변화 중심)")
        String headline,
        @JsonPropertyDescription("운영 상황 서술 3~5문장 — 제공된 집계 숫자만 인용, 추측 금지")
        String narrative,
        @JsonPropertyDescription("관리자가 즉시 조치해야 할 항목 (없으면 빈 배열)")
        List<BriefingItem> actionRequired,
        @JsonPropertyDescription("당장 조치는 불필요하나 관찰이 필요한 항목")
        List<BriefingItem> watching,
        @JsonPropertyDescription("정상 동작 중임을 확인해주는 항목")
        List<BriefingItem> healthy
) {

    public record BriefingItem(
            @JsonPropertyDescription("항목 제목 (10자 내외)")
            String title,
            @JsonPropertyDescription("한 줄 상세 설명")
            String detail,
            @JsonPropertyDescription("관련 카테고리 코드 (authn_attack, http_anomaly, enrollment 등)")
            String relatedCategory
    ) {}
}
