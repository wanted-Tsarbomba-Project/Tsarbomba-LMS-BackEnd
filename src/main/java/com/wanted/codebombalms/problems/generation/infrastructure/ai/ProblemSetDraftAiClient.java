package com.wanted.codebombalms.problems.generation.infrastructure.ai;

import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.generation.infrastructure.ai.request.FastApiProblemSetDraftRequest;
import com.wanted.codebombalms.problems.generation.infrastructure.ai.response.FastApiProblemSetDraftResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class ProblemSetDraftAiClient {

    private static final String GENERATE_PATH = "/problem-set-draft/chat";
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 60_000;

    private final RestClient restClient;

    public ProblemSetDraftAiClient(
            @Value("${fastapi.url}") String fastApiBaseUrl,
            RestClient.Builder restClientBuilder
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MS);

        this.restClient = restClientBuilder
                .baseUrl(fastApiBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public FastApiProblemSetDraftResponse generate(FastApiProblemSetDraftRequest request) {
        long startedAt = System.nanoTime();

        try {
            FastApiProblemSetDraftResponse response = restClient.post()
                    .uri(GENERATE_PATH)
                    .body(request)
                    .retrieve()
                    .body(FastApiProblemSetDraftResponse.class);

            if (response == null) {
                throw new ExternalServiceException(
                        ProblemErrorCode.PROBLEM_SET_DRAFT_GENERATION_FAILED
                );
            }

            log.info(
                    "event=problem_set_draft_ai_completed problemCount={} subProblemCount={} durationMs={}",
                    request.problemCount(),
                    request.subProblemCount(),
                    elapsedMillis(startedAt)
            );

            return response;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "event=problem_set_draft_ai_failed exceptionType={} durationMs={}",
                    e.getClass().getSimpleName(),
                    elapsedMillis(startedAt),
                    e
            );

            throw new ExternalServiceException(
                    ProblemErrorCode.PROBLEM_SET_DRAFT_GENERATION_FAILED,
                    e
            );
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
