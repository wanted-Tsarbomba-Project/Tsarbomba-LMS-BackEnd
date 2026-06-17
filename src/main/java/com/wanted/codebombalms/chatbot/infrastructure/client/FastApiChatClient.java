package com.wanted.codebombalms.chatbot.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient;
import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!mock")
public class FastApiChatClient implements AiChatClient {

    private static final String EVENT_DONE = "done";
    private static final String EVENT_ERROR = "error";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<AiChatStreamChunk> stream(ChatContext context) {
        FastApiChatRequest request = toRequest(context);

        return webClient.post()
                .uri("/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .map(this::toChunk)
                .onErrorResume(e -> {
                    log.error("FastAPI 스트리밍 호출 실패", e);
                    return Flux.just(new AiChatStreamChunk.Error(
                            ChatErrorCode.AI_RESPONSE_FAILED.getCode(),
                            ChatErrorCode.AI_RESPONSE_FAILED.getMessage()
                    ));
                });
    }

    /** FastAPI SSE 프레임 한 개 → 도메인 청크. event 이름으로 분기(없으면 토큰). */
    private AiChatStreamChunk toChunk(ServerSentEvent<String> sse) {
        String event = sse.event();
        String data = sse.data() == null ? "" : sse.data();
        try {
            if (EVENT_DONE.equals(event)) {
                JsonNode n = objectMapper.readTree(data);
                return new AiChatStreamChunk.Done(new AiChatStreamChunk.TokenUsage(
                        n.path("promptTokens").asInt(0),
                        n.path("completionTokens").asInt(0),
                        n.path("totalTokens").asInt(0)
                ));
            }
            if (EVENT_ERROR.equals(event)) {
                JsonNode n = objectMapper.readTree(data);
                return new AiChatStreamChunk.Error(
                        n.path("code").asText(ChatErrorCode.AI_RESPONSE_FAILED.getCode()),
                        n.path("message").asText(ChatErrorCode.AI_RESPONSE_FAILED.getMessage())
                );
            }
            // event 없음 = 본문 토큰: {"t":"..."}
            return new AiChatStreamChunk.Token(objectMapper.readTree(data).path("t").asText(""));
        } catch (Exception e) {
            log.error("FastAPI SSE 프레임 파싱 실패 - event={}, data={}", event, data, e);
            return new AiChatStreamChunk.Error(
                    ChatErrorCode.AI_RESPONSE_FAILED.getCode(),
                    ChatErrorCode.AI_RESPONSE_FAILED.getMessage()
            );
        }
    }

    private FastApiChatRequest toRequest(ChatContext context) {
        List<FastApiChatRequest.MessageDto> history = context.conversationHistory().stream()
                .map(m -> FastApiChatRequest.MessageDto.builder()
                        .role(m.getRole().name().toLowerCase())
                        .content(m.getContent())
                        .build())
                .collect(Collectors.toList());

        List<FastApiChatRequest.ProblemDto> problems = context.problemInfos().stream()
                .map(p -> FastApiChatRequest.ProblemDto.builder()
                        .title(p.title())
                        .content(p.content())
                        .problemType(p.problemType())
                        .explanation(p.explanation())
                        .submittedCode(p.submittedCode())
                        .build())
                .collect(Collectors.toList());

        ChatContextPort.ProblemSetInfo ps = context.problemSetInfo();
        FastApiChatRequest.ProblemSetDto problemSetDto = ps == null ? null :
                FastApiChatRequest.ProblemSetDto.builder()
                        .problemSetId(ps.problemSetId())
                        .title(ps.title())
                        .description(ps.description())
                        .build();

        ChatContextPort.SessionProgressInfo sp = context.sessionProgressInfo();
        FastApiChatRequest.SessionProgressDto sessionProgressDto = sp == null ? null :
                FastApiChatRequest.SessionProgressDto.builder()
                        .currentProblemNumber(sp.currentProblemNumber())
                        .build();

        ChatContextPort.DatasetInfo di = context.datasetInfo();
        FastApiChatRequest.DatasetDto datasetDto = di == null ? null :
                FastApiChatRequest.DatasetDto.builder()
                        .metaData(di.metaData())
                        .build();

        return FastApiChatRequest.builder()
                .userMessage(context.userMessage())
                .problemSet(problemSetDto)
                .problems(problems.isEmpty() ? null : problems)
                .sessionProgress(sessionProgressDto)
                .dataset(datasetDto)
                .conversationHistory(history)
                .build();
    }
}
