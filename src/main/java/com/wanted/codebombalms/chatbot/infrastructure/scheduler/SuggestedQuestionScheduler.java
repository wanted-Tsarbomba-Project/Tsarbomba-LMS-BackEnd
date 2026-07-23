package com.wanted.codebombalms.chatbot.infrastructure.scheduler;

import com.wanted.codebombalms.chatbot.application.usecase.GenerateSuggestedQuestionsUseCase;
import com.wanted.codebombalms.recommendation.infrastructure.scheduler.RecommendationBatchLockExecutor;
import java.util.OptionalInt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 새벽 문제별 추천 질문 생성 배치를 실행한다.
 * NOTE: 분산락은 recommendation 의 named-lock executor 를 재사용한다(원래는 global 로 승격이 이상적).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuggestedQuestionScheduler {

    private static final String LOCK_NAME = "suggestedQuestionGeneration";

    private final GenerateSuggestedQuestionsUseCase generateUseCase;
    private final RecommendationBatchLockExecutor lockExecutor;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void generateDailySuggestedQuestions() {
        try {
            OptionalInt savedCount = lockExecutor.executeIfLockAcquired(LOCK_NAME, generateUseCase::generate);

            if (savedCount.isEmpty()) {
                log.info("다른 서버에서 추천 질문 생성 배치가 실행 중이라 이번 실행을 건너뜁니다.");
                return;
            }

            log.info("추천 질문 생성 배치를 완료했습니다. savedProblems={}", savedCount.getAsInt());
        } catch (Exception e) {
            log.error("추천 질문 생성 배치 중 예외가 발생했습니다.", e);
        }
    }
}
