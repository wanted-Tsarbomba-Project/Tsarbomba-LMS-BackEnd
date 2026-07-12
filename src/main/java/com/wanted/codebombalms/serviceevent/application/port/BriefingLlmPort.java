package com.wanted.codebombalms.serviceevent.application.port;

import com.wanted.codebombalms.serviceevent.domain.model.BriefingContent;
import java.time.LocalDateTime;

/**
 * AI 브리핑 생성 포트.
 * PII 미반출 — input 은 집계 수치·마스킹 IP 라벨만 (user_id·이메일·원본 IP 금지).
 */
public interface BriefingLlmPort {

    /** 집계 스냅샷으로 브리핑 생성 — 실패는 예외 전파, FAILED 기록은 호출측 담당 */
    BriefingContent generate(BriefingSource source);

    /** 사용 중인 모델명 (ops_briefing.model 기록용) */
    String modelName();

    /**
     * 프롬프트 재료 — 이미 집계·마스킹이 끝난 문자열/숫자만 담는 봉투.
     * @param aggregatesText 사람이 읽을 수 있는 집계 요약 텍스트 (마스킹 완료본)
     */
    record BriefingSource(
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            String aggregatesText
    ) {}
}
