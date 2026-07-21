package com.wanted.codebombalms.serviceevent.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanted.codebombalms.serviceevent.presentation.api.request.OpsChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * FastAPI 운영 Q&A 챗봇(/ops-chat) 릴레이 클라이언트.
 *
 * 학생 챗봇(FastApiChatClient)과 달리 프레임을 도메인 모델로 파싱하지 않고
 * SSE 를 그대로 재송출한다 — event 이름(status/done/error)과 토큰 프레임을
 * FE 가 FastAPI 계약 그대로 받는다. (메시지 영속화·토큰 집계가 없어 파싱 불필요)
 *
 * WebClient 는 chatbot WebClientConfig 의 전역 빈(baseUrl=fastapi.url) 재사용 —
 * 같은 FastAPI 서버의 다른 엔드포인트라 설정이 동일하다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpsChatFastApiClient {

    // FastAPI ops_gemini_client.py 의 OPS_RESPONSE_FAILED 와 공유하는 계약
    private static final String OPS_RESPONSE_FAILED_CODE = "OPS-001";
    private static final String CALL_FAILED_DATA =
            "{\"code\": \"" + OPS_RESPONSE_FAILED_CODE + "\", \"message\": \"운영 챗봇 호출에 실패했습니다.\"}";

    private final WebClient webClient;

    public Flux<ServerSentEvent<String>> stream(OpsChatRequest request, String traceId) {
        return webClient.post()
                .uri("/ops-chat")
                // BE↔FastAPI 로그를 같은 traceId 로 엮는다 (학생 챗봇과 동일 컨벤션)
                .headers(h -> {
                    if (traceId != null) {
                        h.set("X-Trace-Id", traceId);
                    }
                })
                .bodyValue(toFastApiRequest(request))
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .map(this::passThrough)
                .onErrorResume(e -> {
                    log.warn("event=opschat_ai_call_failed traceId={} - FastAPI 운영 챗봇 호출 실패", traceId, e);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data(CALL_FAILED_DATA)
                            .build());
                });
    }

    /** 수신 프레임을 event 이름 보존 채로 재조립 — 내용은 손대지 않는다. */
    private ServerSentEvent<String> passThrough(ServerSentEvent<String> sse) {
        ServerSentEvent.Builder<String> builder = ServerSentEvent.builder(sse.data() == null ? "" : sse.data());
        if (sse.event() != null) {
            builder.event(sse.event());
        }
        return builder.build();
    }

    private FastApiOpsChatRequest toFastApiRequest(OpsChatRequest request) {
        List<FastApiOpsChatRequest.MessageDto> history = request.conversationHistory() == null
                ? null
                : request.conversationHistory().stream()
                        .map(m -> new FastApiOpsChatRequest.MessageDto(m.role(), m.content()))
                        .toList();
        return new FastApiOpsChatRequest(request.userMessage(), history);
    }

    /** FastAPI OpsChatRequest(snake_case) 와의 전송 계약. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record FastApiOpsChatRequest(
            @JsonProperty("user_message") String userMessage,
            @JsonProperty("conversation_history") List<MessageDto> conversationHistory
    ) {
        record MessageDto(String role, String content) {
        }
    }
}
