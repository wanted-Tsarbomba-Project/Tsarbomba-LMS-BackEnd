package com.wanted.codebombalms.serviceevent.application.port;

import com.wanted.codebombalms.serviceevent.domain.model.BriefingContent;
import java.time.LocalDateTime;

/**
 * AI 브리핑 생성 포트 (#609). 구현은 infrastructure(Claude SDK)에 두고,
 * 모델 교체는 설정(${BRIEFING_MODEL})만 바꾸면 된다.
 *
 * <p>PII 정책: input 에는 집계 수치·마스킹된 IP 라벨만 담는다.
 * user_id·이메일·원본 IP 는 절대 포함하지 않는다 (설계서 §8-2).
 */
public interface BriefingLlmPort {

    /**
     * 집계 스냅샷으로 브리핑을 생성한다.
     * 실패(네트워크·refusal 포함)는 예외로 던진다 — 호출측이 FAILED 기록을 담당.
     */
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
