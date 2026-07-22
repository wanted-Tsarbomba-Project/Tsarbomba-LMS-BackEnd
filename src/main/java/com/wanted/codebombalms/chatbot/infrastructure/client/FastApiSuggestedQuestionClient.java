package com.wanted.codebombalms.chatbot.infrastructure.client;

import com.wanted.codebombalms.chatbot.application.model.GeneratedProblemQuestions;
import com.wanted.codebombalms.chatbot.application.model.ProblemQuestionsGroup;
import com.wanted.codebombalms.chatbot.application.port.SuggestedQuestionGenerationClient;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/** 추천 질문 생성 전용 FastAPI 엔드포인트를 호출하는 client. (요청/응답 camelCase) */
@Slf4j
@Component
public class FastApiSuggestedQuestionClient implements SuggestedQuestionGenerationClient {

    private static final String GENERATE_PATH = "/internal/suggested-questions/generate";
    private static final int MAX_RESPONSE_IN_MEMORY_BYTES = 2 * 1024 * 1024;
    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 300000;

    @Value("${fastapi.url}")
    private String fastApiBaseUrl;

    @Override
    public List<GeneratedProblemQuestions> generate(List<ProblemQuestionsGroup> problems, int topN) {
        if (problems.isEmpty()) {
            return List.of();
        }

        PythonRequest requestBody = new PythonRequest(
                problems.stream()
                        .map(problem -> new PythonProblem(problem.problemSetId(), problem.problemId(), problem.questions()))
                        .toList(),
                topN
        );

        try {
            PythonResponse response = webClient().post()
                    .uri(GENERATE_PATH)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(PythonResponse.class)
                    .block();

            if (response == null || response.problems() == null) {
                return List.of();
            }

            return response.problems().stream()
                    .map(problem -> new GeneratedProblemQuestions(
                            problem.problemSetId(),
                            problem.problemId(),
                            problem.questions() == null ? List.of() : problem.questions()
                    ))
                    .toList();
        } catch (RuntimeException exception) {
            log.warn("event=suggested_question_generation_failed exceptionType={}",
                    exception.getClass().getSimpleName());
            throw exception;
        }
    }

    private WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .responseTimeout(Duration.ofMillis(READ_TIMEOUT_MS));

        return WebClient.builder()
                .baseUrl(fastApiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_RESPONSE_IN_MEMORY_BYTES))
                .build();
    }

    /** FastAPI 요청 (camelCase). */
    private record PythonRequest(List<PythonProblem> problems, int topN) {
    }

    /** 요청·응답 공용 문제 묶음 (camelCase). */
    private record PythonProblem(Long problemSetId, Long problemId, List<String> questions) {
    }

    /** FastAPI 응답 (camelCase). */
    private record PythonResponse(List<PythonProblem> problems) {
    }
}
