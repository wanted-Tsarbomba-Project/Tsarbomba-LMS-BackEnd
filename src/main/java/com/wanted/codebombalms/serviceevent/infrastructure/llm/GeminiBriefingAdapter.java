package com.wanted.codebombalms.serviceevent.infrastructure.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
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
 * Gemini API 브리핑 어댑터 — 기본 프로바이더.
 * JSON 모드 + 프롬프트 스키마로 BriefingContent 유도, 파싱·검증 실패는 생성 실패(FAILED) 처리.
 * 키 미설정 시 부팅 정상·생성만 실패.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "briefing.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiBriefingAdapter implements BriefingLlmPort {

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    /** timeout — 공급자 지연 시 스레드 점유 방지 */
    private static final int TIMEOUT_MS = 120_000;

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

    private static final String SCHEMA_GUIDE = """
            반드시 아래 JSON 스키마 형태로만 응답하세요 (마크다운·설명 없이 JSON 객체 하나만):
            {
              "headline": "가장 중요한 변화 한 문장",
              "narrative": "운영 상황 서술 3~5문장",
              "actionRequired": [ { "title": "항목 제목", "detail": "한 줄 상세", "relatedCategory": "카테고리 코드" } ],
              "watching": [ { "title": "...", "detail": "...", "relatedCategory": "..." } ],
              "healthy": [ { "title": "...", "detail": "...", "relatedCategory": "..." } ]
            }
            조치할 것이 없으면 actionRequired 는 빈 배열 [] 로 둡니다.
            """;

    private final Client client;
    private final String model;
    private final ObjectMapper objectMapper;

    public GeminiBriefingAdapter(
            @Value("${briefing.api-key:}") String apiKey,
            @Value("${briefing.model:gemini-3.5-flash}") String model,
            ObjectMapper objectMapper) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.client = (apiKey == null || apiKey.isBlank())
                ? null
                : Client.builder()
                        .apiKey(apiKey)
                        .httpOptions(HttpOptions.builder().timeout(TIMEOUT_MS).build())
                        .build();
        if (this.client == null) {
            log.warn("event=briefing_llm_disabled provider=gemini reason=api_key_not_set — 브리핑 생성 요청은 실패 처리됩니다");
        }
    }

    @Override
    public BriefingContent generate(BriefingSource source) {
        if (client == null) {
            throw new ExternalServiceException(ServiceEventErrorCode.BRIEFING_GENERATION_FAILED);
        }
        try {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response =
                    client.models.generateContent(model, buildPrompt(source), config);

            String json = response.text();
            if (json == null || json.isBlank()) {
                throw new ExternalServiceException(ServiceEventErrorCode.BRIEFING_GENERATION_FAILED);
            }

            BriefingContent content = objectMapper.readValue(json, BriefingContent.class);
            validate(content);
            return content;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("event=briefing_generation_failed provider=gemini model={}", model, e);
            throw new ExternalServiceException(ServiceEventErrorCode.BRIEFING_GENERATION_FAILED, e);
        }
    }

    @Override
    public String modelName() {
        return model;
    }

    private String buildPrompt(BriefingSource source) {
        return SYSTEM_PROMPT + "\n"
                + SCHEMA_GUIDE + "\n"
                + """
                다음은 %s ~ %s 구간의 서비스 이벤트 집계입니다.
                이 데이터만 근거로 운영 브리핑을 작성하세요.

                %s
                """.formatted(
                source.periodStart().format(PERIOD_FORMAT),
                source.periodEnd().format(PERIOD_FORMAT),
                source.aggregatesText());
    }

    /** JSON 모드는 스키마 비강제 — 필수 필드 수동 검증 */
    private void validate(BriefingContent content) {
        if (content == null
                || content.headline() == null || content.headline().isBlank()
                || content.narrative() == null || content.narrative().isBlank()
                || content.actionRequired() == null
                || content.watching() == null
                || content.healthy() == null) {
            throw new ExternalServiceException(ServiceEventErrorCode.BRIEFING_GENERATION_FAILED);
        }
    }
}
