package com.wanted.codebombalms.chatbot.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FastApiChatRequest {

    @JsonProperty("user_message")
    private String userMessage;

    @JsonProperty("problem_set_id")
    private Long problemSetId;

    @JsonProperty("problem_id")
    private Long problemId;

    @JsonProperty("conversation_history")
    private List<MessageDto> conversationHistory;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MessageDto {
        private String role;
        private String content;
    }
}