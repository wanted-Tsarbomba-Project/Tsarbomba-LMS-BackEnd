package com.wanted.codebombalms.chatbot.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient.AiChatClientResponse;
import com.wanted.codebombalms.chatbot.application.port.ChatContextPort.*;
import com.wanted.codebombalms.chatbot.domain.model.ChatMessage;
import com.wanted.codebombalms.chatbot.domain.model.MessageRole;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FastApiChatClientTest {

    private MockWebServer mockWebServer;
    private FastApiChatClient fastApiChatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        fastApiChatClient = new FastApiChatClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private void enqueueSuccessResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "answer": "AI 응답입니다",
                            "is_answer_detected": true,
                            "retry_count": 0,
                            "prompt_tokens": 100,
                            "completion_tokens": 50,
                            "total_tokens": 150
                        }
                        """));
    }

    // ── 케이스 4: 풀 컨텍스트 → JSON 모든 필드 존재 ──

    @Test
    void 풀_컨텍스트_전송시_JSON_모든_필드_존재() throws Exception {
        enqueueSuccessResponse();

        ChatContext context = new ChatContext(
                1L, 100L, "질문입니다",
                new ProblemSetInfo(10L, "문제집", "설명"),
                List.of(new ProblemInfo("문제1", "내용1", "TEXT", "정답1", "해설1", "제출답1")),
                new SessionProgressInfo(3),
                new DatasetInfo("meta data"),
                List.of(ChatMessage.restore(1L, 100L, MessageRole.USER, "이전 질문", Instant.now()))
        );

        AiChatClientResponse response = fastApiChatClient.call(context);

        // 응답 검증
        assertThat(response.answer()).isEqualTo("AI 응답입니다");
        assertThat(response.isAnswerDetected()).isTrue();

        // 요청 JSON 검증
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/chat");
        assertThat(request.getMethod()).isEqualTo("POST");

        JsonNode json = objectMapper.readTree(request.getBody().readUtf8());

        // user_message
        assertThat(json.get("user_message").asText()).isEqualTo("질문입니다");

        // problem_set
        JsonNode ps = json.get("problem_set");
        assertThat(ps.get("problem_set_id").asLong()).isEqualTo(10L);
        assertThat(ps.get("title").asText()).isEqualTo("문제집");
        assertThat(ps.get("description").asText()).isEqualTo("설명");

        // problems
        JsonNode problems = json.get("problems");
        assertThat(problems).hasSize(1);
        assertThat(problems.get(0).get("title").asText()).isEqualTo("문제1");
        assertThat(problems.get(0).get("problem_type").asText()).isEqualTo("TEXT");
        assertThat(problems.get(0).get("submitted_answer").asText()).isEqualTo("제출답1");

        // session_progress
        assertThat(json.get("session_progress").get("current_problem_number").asInt()).isEqualTo(3);

        // dataset
        assertThat(json.get("dataset").get("meta_data").asText()).isEqualTo("meta data");

        // conversation_history
        JsonNode history = json.get("conversation_history");
        assertThat(history).hasSize(1);
        assertThat(history.get(0).get("role").asText()).isEqualTo("user");
        assertThat(history.get(0).get("content").asText()).isEqualTo("이전 질문");
    }

    // ── 케이스 5: 자유 질문 모드 → null 필드 JSON에서 빠짐 ──

    @Test
    void 자유질문모드_null_필드_JSON에서_제외됨() throws Exception {
        enqueueSuccessResponse();

        ChatContext context = new ChatContext(
                1L, 100L, "자유 질문",
                null,           // problemSetInfo
                List.of(),      // problemInfos → empty → null로 변환됨
                null,           // sessionProgressInfo
                null,           // datasetInfo
                List.of()       // conversationHistory → empty list
        );

        fastApiChatClient.call(context);

        RecordedRequest request = mockWebServer.takeRequest();
        JsonNode json = objectMapper.readTree(request.getBody().readUtf8());

        // user_message 존재
        assertThat(json.get("user_message").asText()).isEqualTo("자유 질문");

        // null 필드들 JSON에 없어야 함 (@JsonInclude NON_NULL)
        assertThat(json.has("problem_set")).isFalse();
        assertThat(json.has("problems")).isFalse();
        assertThat(json.has("session_progress")).isFalse();
        assertThat(json.has("dataset")).isFalse();

        // conversation_history는 빈 배열로 존재 (null 아님)
        assertThat(json.get("conversation_history")).isEmpty();
    }
}
