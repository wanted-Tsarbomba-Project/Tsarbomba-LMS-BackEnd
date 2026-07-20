package com.wanted.codebombalms.learning.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningContext;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningProfile;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningRecommendationRequest;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LectureContext;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FastApiLearningRecommendationClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockWebServer server;
    private FastApiLearningRecommendationClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        LearningRecommendationProperties properties = new LearningRecommendationProperties();
        client = new FastApiLearningRecommendationClient(
                properties,
                server.url("/").toString()
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void rankFinalProblemSets_matchesPythonJsonContract() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "algorithm": "GEMINI_EMBEDDING_PERSONALIZED",
                          "recommendations": [
                            {
                              "problemSetId": 53,
                              "score": 0.8914,
                              "reasonCode": "REVIEW_WEAK_AREA",
                              "recommendationReason": "해설을 확인한 학습 기록을 바탕으로 복습용 문제를 추천해요."
                            }
                          ]
                        }
                        """));

        var result = client.rankFinalProblemSets(request());

        assertEquals(1, result.recommendations().size());
        assertEquals(53L, result.recommendations().get(0).problemSetId());
        assertEquals("REVIEW_WEAK_AREA", result.recommendations().get(0).reasonCode().name());

        RecordedRequest recorded = server.takeRequest();
        assertEquals("/internal/learning/final-problem-sets/rank", recorded.getPath());
        JsonNode body = objectMapper.readTree(recorded.getBody().readUtf8());
        assertEquals(20L, body.get("courseId").asLong());
        assertEquals(30L, body.get("lectureId").asLong());
        assertEquals(3106L, body.get("problemCategoryId").asLong());
        assertEquals(1001L, body.get("excludedProblemSetIds").get(0).asLong());
        assertEquals(10, body.get("learningProfile").get("totalMainProblemCount").asInt());
        assertEquals("Python", body.get("learningContext").get("courseTitle").asText());
    }

    @Test
    void rankFinalProblemSets_rejectsInvalidPythonResponse() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "algorithm": "GEMINI_EMBEDDING_PERSONALIZED",
                          "recommendations": [
                            {
                              "problemSetId": 53,
                              "score": 1.5,
                              "reasonCode": "COURSE_RELATED",
                              "recommendationReason": "추천 문구예요."
                            }
                          ]
                        }
                        """));

        assertThrows(ExternalServiceException.class, () ->
                client.rankFinalProblemSets(request())
        );
    }

    private LearningRecommendationRequest request() {
        return new LearningRecommendationRequest(
                20L,
                30L,
                3106L,
                List.of(1001L),
                2,
                new LearningProfile(10, 6, 60.0, 3, 30.0, 2.4, 72.5),
                new LearningContext(
                        "Python",
                        "Python basics",
                        List.of(new LectureContext("Loop", "for and while"))
                )
        );
    }
}
