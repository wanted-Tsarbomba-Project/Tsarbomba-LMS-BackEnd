package com.wanted.codebombalms.recommendation.infrastructure.client;

import com.wanted.codebombalms.recommendation.application.command.GeneratedProblemSetRecommendation;
import com.wanted.codebombalms.recommendation.application.command.GeneratedUserProblemSetRecommendations;
import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationGenerationClient;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationAlgorithm;
import io.netty.channel.ChannelOption;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/** 추천 시스템 전용 Python FastAPI endpoint를 호출하는 client입니다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiProblemRecommendationGenerationClient implements ProblemRecommendationGenerationClient {

    private final RecommendationPythonProperties properties;

    @Value("${fastapi.url}")
    private String fastApiBaseUrl;

    /** 설정된 Python 추천 생성 endpoint를 호출해 사용자별 추천 결과를 가져옵니다. */
    @Override
    public List<GeneratedUserProblemSetRecommendations> generateProblemSetRecommendations() {
        if (!properties.isEnabled()) {
            log.info("Python 추천 생성 호출이 비활성화되어 추천 생성 배치를 건너뜁니다.");
            return List.of();
        }

        PythonRecommendationResponse response = webClient().post()
                .uri(properties.getGeneratePath())
                .bodyValue(new PythonRecommendationRequest(3))
                .retrieve()
                .bodyToMono(PythonRecommendationResponse.class)
                .block();

        if (response == null || response.recommendations() == null) {
            return List.of();
        }

        return response.recommendations()
                .stream()
                .map(PythonUserRecommendations::toCommand)
                .toList();
    }

    /** 추천 기능이 챗봇 WebClient 설정에 의존하지 않도록 전용 client를 구성합니다. */
    private WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));

        return WebClient.builder()
                .baseUrl(fastApiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /** Python 서버에 요청하는 추천 개수 조건입니다. */
    private record PythonRecommendationRequest(int recommendationCount) {
    }

    /** Python 추천 생성 API의 최상위 응답입니다. */
    private record PythonRecommendationResponse(List<PythonUserRecommendations> recommendations) {
    }

    /** Python 추천 생성 API의 사용자별 추천 묶음입니다. */
    private record PythonUserRecommendations(Long userId, List<PythonProblemSetRecommendation> problemSets) {

        private GeneratedUserProblemSetRecommendations toCommand() {
            return new GeneratedUserProblemSetRecommendations(
                    userId,
                    normalizedProblemSets().stream()
                            .map(PythonProblemSetRecommendation::toCommand)
                            .toList()
            );
        }

        /** 외부 응답의 null 목록은 서비스 계층의 개수 검증으로 넘기기 위해 빈 목록으로 정규화합니다. */
        private List<PythonProblemSetRecommendation> normalizedProblemSets() {
            return problemSets == null ? List.of() : problemSets;
        }
    }

    /** Python 추천 생성 API의 문제 세트 추천 한 건입니다. */
    private record PythonProblemSetRecommendation(
            Long problemSetId,
            BigDecimal support,
            BigDecimal confidence,
            BigDecimal lift,
            Integer rankNo,
            RecommendationAlgorithm algorithm
    ) {

        private GeneratedProblemSetRecommendation toCommand() {
            return new GeneratedProblemSetRecommendation(
                    problemSetId,
                    support,
                    confidence,
                    lift,
                    rankNo,
                    algorithm == null ? RecommendationAlgorithm.APRIORI : algorithm
            );
        }
    }
}
