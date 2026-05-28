package com.wanted.codebombalms.chatbot.infrastructure.client;

import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient;
import com.wanted.codebombalms.chatbot.application.port.ChatContextPort;
import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!mock")
public class FastApiChatClient implements AiChatClient {

    private final WebClient webClient;

    @Override
    public AiChatClientResponse call(ChatContext context) {
        FastApiChatRequest request = toRequest(context);

        try {
            FastApiChatResponse response = webClient.post()
                    .uri("/chat")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(FastApiChatResponse.class)
                    .block();

            return toClientResponse(response);
        } catch (WebClientResponseException e) {
            log.error("FastAPI 호출 실패 - status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException(ChatErrorCode.AI_RESPONSE_FAILED);
        } catch (Exception e) {
            log.error("FastAPI 호출 중 예외 발생", e);
            throw new ExternalServiceException(ChatErrorCode.AI_RESPONSE_FAILED);
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
                        .answer(p.answer())
                        .explanation(p.explanation())
                        .submittedAnswer(p.submittedAnswer())
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

    private AiChatClientResponse toClientResponse(FastApiChatResponse response) {
        return new AiChatClientResponse(
                response.getAnswer(),
                response.isAnswerDetected(),
                response.getRetryCount(),
                new TokenUsage(
                        response.getPromptTokens(),
                        response.getCompletionTokens(),
                        response.getTotalTokens()
                )
        );
    }
}
