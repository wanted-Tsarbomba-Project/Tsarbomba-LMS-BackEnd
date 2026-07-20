package com.wanted.codebombalms.inquiry.infrastructure.client;

import com.wanted.codebombalms.inquiry.application.command.ApplyInquiryAiAnalysisCommand;
import com.wanted.codebombalms.inquiry.application.command.CorrectionExample;
import com.wanted.codebombalms.inquiry.application.command.RequestInquiryAnalysisCommand;
import com.wanted.codebombalms.inquiry.application.port.InquiryAnalysisClient;
import com.wanted.codebombalms.inquiry.application.usecase.ApplyInquiryAiAnalysisUseCase;
import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import io.netty.channel.ChannelOption;
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

/** 문의 원문을 Python AI 분석 endpoint로 비동기 전달하고, 응답을 받아 문의에 반영하는 client입니다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiInquiryAnalysisClient implements InquiryAnalysisClient {

    private final InquiryPythonProperties properties;
    private final ApplyInquiryAiAnalysisUseCase applyInquiryAiAnalysisUseCase;

    @Value("${fastapi.url}")
    private String fastApiBaseUrl;

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
            PythonInquiryAnalysisResponse response = webClient().post()
                    .uri(properties.getAnalyzePath())
                    .bodyValue(new PythonInquiryAnalysisRequest(
                            command.inquiryId(),
                            command.userId(),
                            command.sourceUrl(),
                            command.content(),
                            command.correctionExamples()
                    ))
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

    /** 문의 기능이 다른 도메인의 WebClient 설정에 의존하지 않도록 전용 client를 구성합니다. */
    private WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));

        return WebClient.builder()
                .baseUrl(fastApiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /** Python 문의 분석 API에 전달하는 요청 본문입니다. */
    private record PythonInquiryAnalysisRequest(
            Long inquiryId,
            Long userId,
            String sourceUrl,
            String content,
            List<CorrectionExample> correctionExamples
    ) {
    }

    /** Python 문의 분석 API의 응답 본문입니다. */
    private record PythonInquiryAnalysisResponse(
            String title,
            String summary,
            InquirySeverity severity,
            InquiryDomain domain,
            String estimatedUrl,
            String recommendedAction,
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
