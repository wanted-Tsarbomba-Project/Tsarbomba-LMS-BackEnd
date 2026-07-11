package com.wanted.codebombalms.serviceevent.infrastructure.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.serviceevent.application.port.BriefingLlmPort;
import com.wanted.codebombalms.serviceevent.domain.exception.ServiceEventErrorCode;
import com.wanted.codebombalms.serviceevent.domain.model.BriefingContent;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Claude API 브리핑 어댑터 (#609).
 *
 * <p>구조화 출력(outputConfig)으로 응답이 BriefingContent 스키마를 강제받는다 — 파싱 실패 원천 차단.
 * 모델은 ${BRIEFING_MODEL}(기본 claude-opus-4-8), 키는 ${ANTHROPIC_API_KEY} — 키 미설정 시
 * 부팅은 정상, 생성 요청만 실패(FAILED 기록)한다.
 * 보안 이벤트 요약은 안전 분류기가 드물게 거절(stop_reason=refusal)할 수 있어
 * 프롬프트를 "방어 관점 운영 보고서"로 프레이밍하고 refusal 은 생성 실패로 처리한다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "briefing.provider", havingValue = "anthropic")
public class AnthropicBriefingAdapter implements BriefingLlmPort {

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private static final String SYSTEM_PROMPT = """
            당신은 온라인 코딩 교육 플랫폼 '코드봄바'의 보안·운영 브리핑 작성자다.
            관리자(방어자)가 서비스 상태를 빠르게 파악하고 계정 보호 조치를 하도록 돕는 것이 목적이다.
            규칙:
            - 제공된 집계 수치만 인용한다. 제공되지 않은 사실·수치를 만들어내지 않는다.
            - 공격 기법의 실행 방법은 절대 서술하지 않는다. 방어와 조치 관점으로만 쓴다.
            - 서로 다른 신호가 같은 시간대·같은 맥락으로 겹치면 연결해서 진단한다.
            - 이상 신호가 없으면 없다고 명확히 쓰고 healthy 를 채운다. 과장 금지.
            - 한국어 경어체(~습니다)로 작성한다.
            """;

    private final AnthropicClient client;
    private final String model;

    public AnthropicBriefingAdapter(
            @Value("${briefing.api-key:}") String apiKey,
            @Value("${briefing.model:claude-opus-4-8}") String model) {
        this.model = model;
        this.client = (apiKey == null || apiKey.isBlank())
                ? null
                : AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        if (this.client == null) {
            log.warn("event=briefing_llm_disabled reason=api_key_not_set — 브리핑 생성 요청은 실패 처리됩니다");
        }
    }

    @Override
    public BriefingContent generate(BriefingSource source) {
        if (client == null) {
            throw new ExternalServiceException(ServiceEventErrorCode.BRIEFING_GENERATION_FAILED);
        }
        try {
            var params = MessageCreateParams.builder()
                    .model(model)
                    .maxTokens(8000L)
                    .thinking(ThinkingConfigAdaptive.builder().build())
                    .system(SYSTEM_PROMPT)
                    .outputConfig(BriefingContent.class)
                    .addUserMessage(buildUserPrompt(source))
                    .build();

            var message = client.messages().create(params);

            boolean refused = message.stopReason()
                    .map(StopReason.REFUSAL::equals)
                    .orElse(false);
            if (refused) {
                log.warn("event=briefing_generation_refused model={}", model);
                throw new ExternalServiceException(ServiceEventErrorCode.BRIEFING_GENERATION_FAILED);
            }

            return message.content().stream()
                    .flatMap(block -> block.text().stream())
                    .findFirst()
                    .orElseThrow(() -> new ExternalServiceException(
                            ServiceEventErrorCode.BRIEFING_GENERATION_FAILED))
                    .text();
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("event=briefing_generation_failed model={}", model, e);
            throw new ExternalServiceException(ServiceEventErrorCode.BRIEFING_GENERATION_FAILED, e);
        }
    }

    @Override
    public String modelName() {
        return model;
    }

    private String buildUserPrompt(BriefingSource source) {
        return """
                다음은 %s ~ %s 구간의 서비스 이벤트 집계입니다.
                이 데이터만 근거로 운영 브리핑을 작성하세요.

                %s

                작성 지침:
                - headline: 이 구간에서 가장 중요한 변화 한 문장
                - narrative: 3~5문장. 증감·시간대·신호 간 연관을 서술
                - actionRequired: 즉시 조치가 필요한 항목만 (없으면 빈 배열)
                - watching: 관찰 필요 항목
                - healthy: 정상 확인 항목 (비즈니스 지표 포함)
                """.formatted(
                source.periodStart().format(PERIOD_FORMAT),
                source.periodEnd().format(PERIOD_FORMAT),
                source.aggregatesText());
    }
}
