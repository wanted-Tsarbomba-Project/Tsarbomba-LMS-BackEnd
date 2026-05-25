package com.wanted.codebombalms.chatbot.infrastructure.client;

import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Profile("!mock")
public class FastApiChatClient implements AiChatClient {

    private final WebClient webClient;

    @Override
    public AiChatClientResponse call(ChatContext context) {
        FastApiChatRequest request = toRequest(context);

        FastApiChatResponse response = webClient.post()
                .uri("/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FastApiChatResponse.class)
                .block();

        return toClientResponse(response);
    }

    private FastApiChatRequest toRequest(ChatContext context) {
        List<FastApiChatRequest.MessageDto> history = context.conversationHistory().stream()
                .map(m -> FastApiChatRequest.MessageDto.builder()
                        .role(m.getRole().name().toLowerCase())
                        .content(m.getContent())
                        .build())
                .collect(Collectors.toList());

        return FastApiChatRequest.builder()
                .userMessage(context.userMessage())
                .problemSetId(context.problemSetId())
                .problemId(context.problemId())
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