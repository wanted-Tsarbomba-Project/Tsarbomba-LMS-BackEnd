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

    @JsonProperty("problem_set")
    private ProblemSetDto problemSet;

    @JsonProperty("problems")
    private List<ProblemDto> problems;

    @JsonProperty("session_progress")
    private SessionProgressDto sessionProgress;

    @JsonProperty("dataset")
    private DatasetDto dataset;

    @JsonProperty("conversation_history")
    private List<MessageDto> conversationHistory;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProblemSetDto {
        @JsonProperty("problem_set_id")
        private Long problemSetId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("description")
        private String description;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProblemDto {
        @JsonProperty("title")
        private String title;

        @JsonProperty("content")
        private String content;

        @JsonProperty("problem_type")
        private String problemType;

        @JsonProperty("answer")
        private String answer;

        @JsonProperty("explanation")
        private String explanation;

        @JsonProperty("submitted_answer")
        private String submittedAnswer;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SessionProgressDto {
        @JsonProperty("current_problem_number")
        private Integer currentProblemNumber;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DatasetDto {
        @JsonProperty("meta_data")
        private String metaData;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MessageDto {
        private String role;
        private String content;
    }
}
