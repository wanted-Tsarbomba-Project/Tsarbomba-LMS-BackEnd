package com.wanted.codebombalms.inquiry.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanted.codebombalms.inquiry.application.command.ApplyInquiryAiAnalysisCommand;
import com.wanted.codebombalms.inquiry.application.command.CorrectionExample;
import com.wanted.codebombalms.inquiry.application.command.RequestInquiryAnalysisCommand;
import com.wanted.codebombalms.inquiry.application.port.InquiryAnalysisClient;
import com.wanted.codebombalms.inquiry.application.usecase.ApplyInquiryAiAnalysisUseCase;
import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import io.netty.channel.ChannelOption;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/** 문의 원문을 Python AI 분석 endpoint로 비동기 전달하고, 응답을 받아 문의에 반영하는 client입니다.
 *  chatbot(FastApiChatRequest)과 동일하게 필드별 {@code @JsonProperty}로 snake_case 계약을 맞춘다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiInquiryAnalysisClient implements InquiryAnalysisClient {

    private final InquiryPythonProperties properties;
    private final ApplyInquiryAiAnalysisUseCase applyInquiryAiAnalysisUseCase;

    @Value("${fastapi.url}")
    private String fastApiBaseUrl;

    private WebClient webClient;

    // @Value 필드 주입이 끝난 뒤(생성자 호출 이후) 커넥션 풀을 가진 WebClient를 한 번만 만들어 재사용한다.
    // 호출마다 새로 만들면 매번 새 커넥션 풀이 생겨 재사용이 안 되고 리소스가 낭비된다.
    @PostConstruct
    private void initWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));

        this.webClient = WebClient.builder()
                .baseUrl(fastApiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /** 문의 등록 요청 스레드를 막지 않도록 전용 executor에서 Python 분석 endpoint를 호출하고, 응답을 받으면 바로 문의에 반영합니다. */
    @Async("inquiryTaskExecutor")
    @Override
    public void analyze(RequestInquiryAnalysisCommand command) {
        if (!properties.isEnabled()) {
            log.info("Python 문의 분석 호출이 비활성화되어 요청을 건너뜁니다. inquiryId={}", command.inquiryId());
            return;
        }

        long startedAt = System.nanoTime();

        try {
            PythonInquiryAnalysisResponse response = webClient.post()
                    .uri(properties.getAnalyzePath())
                    .bodyValue(toRequest(command))
                    .retrieve()
                    .bodyToMono(PythonInquiryAnalysisResponse.class)
                    .block();

            log.info("event=inquiry_ai_analysis_requested inquiryId={} correctionExampleCount={} durationMs={}",
                    command.inquiryId(), command.correctionExamples().size(), (System.nanoTime() - startedAt) / 1_000_000);

            if (response == null) {
                log.warn("event=inquiry_ai_analysis_empty_response inquiryId={}", command.inquiryId());
                return;
            }

            applyInquiryAiAnalysisUseCase.applyAiAnalysis(response.toCommand(command.inquiryId()));
        } catch (RuntimeException exception) {
            log.warn("event=inquiry_ai_analysis_failed inquiryId={} exceptionType={}",
                    command.inquiryId(), exception.getClass().getSimpleName());
        }
    }

    private PythonInquiryAnalysisRequest toRequest(RequestInquiryAnalysisCommand command) {
        return new PythonInquiryAnalysisRequest(
                command.inquiryId(),
                command.userId(),
                command.sourceUrl(),
                command.content(),
                command.correctionExamples().stream().map(PythonCorrectionExample::from).toList()
        );
    }

    /** Python 문의 분석 API에 전달하는 요청 본문. 단어 하나짜리 필드(content)는 어노테이션이 필요 없다. */
    private record PythonInquiryAnalysisRequest(
            @JsonProperty("inquiry_id") Long inquiryId,
            @JsonProperty("user_id") Long userId,
            @JsonProperty("source_url") String sourceUrl,
            String content,
            @JsonProperty("correction_examples") List<PythonCorrectionExample> correctionExamples
    ) {
    }

    /** 관리자 보정 사례 한 건. 애플리케이션 커맨드(CorrectionExample)가 JSON 표현을 몰라도 되도록 어댑터 전용 타입으로 매핑한다. */
    private record PythonCorrectionExample(
            @JsonProperty("field_name") String fieldName,
            @JsonProperty("ai_value") String aiValue,
            @JsonProperty("corrected_value") String correctedValue,
            String reason
    ) {
        private static PythonCorrectionExample from(CorrectionExample example) {
            return new PythonCorrectionExample(
                    example.fieldName().name(),
                    example.aiValue(),
                    example.correctedValue(),
                    example.reason()
            );
        }
    }

    /** Python 문의 분석 API의 응답 본문입니다. */
    private record PythonInquiryAnalysisResponse(
            String title,
            String summary,
            InquirySeverity severity,
            InquiryDomain domain,
            @JsonProperty("estimated_url") String estimatedUrl,
            @JsonProperty("recommended_action") String recommendedAction,
            Boolean filtered
    ) {

        private ApplyInquiryAiAnalysisCommand toCommand(Long inquiryId) {
            return new ApplyInquiryAiAnalysisCommand(
                    inquiryId,
                    title,
                    summary,
                    severity,
                    domain,
                    estimatedUrl,
                    recommendedAction,
                    Boolean.TRUE.equals(filtered)
            );
        }
    }
}
