package com.wanted.codebombalms.chatbot.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FastApiChatResponse {

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("is_answer_detected")
    private boolean isAnswerDetected;

    @JsonProperty("retry_count")
    private int retryCount;

    @JsonProperty("prompt_tokens")
    private int promptTokens;

    @JsonProperty("completion_tokens")
    private int completionTokens;

    @JsonProperty("total_tokens")
    private int totalTokens;
}