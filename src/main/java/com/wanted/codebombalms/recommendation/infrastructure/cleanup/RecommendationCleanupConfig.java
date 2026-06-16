package com.wanted.codebombalms.recommendation.infrastructure.cleanup;

import com.wanted.codebombalms.global.application.cleanup.DefaultHardDeleteTarget;
import com.wanted.codebombalms.global.application.cleanup.port.HardDeleteTarget;
import com.wanted.codebombalms.recommendation.infrastructure.persistence.SpringDataProblemRecommendationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Period;

@Configuration
// 추천 도메인의 하드 딜리트 대상을 공통 cleanup 실행기에 등록합니다.
public class RecommendationCleanupConfig {

    // INACTIVE 상태로 3개월이 지난 문제 세트 추천 row를 하드 딜리트하도록 설정합니다.
    @Bean
    public HardDeleteTarget problemRecommendationHardDeleteTarget(
            SpringDataProblemRecommendationRepository repository
    ) {
        return new DefaultHardDeleteTarget(
                "problem-recommendation",
                Period.ofMonths(3),
                repository::hardDeleteInactiveByUpdatedAtBefore
        );
    }
}
