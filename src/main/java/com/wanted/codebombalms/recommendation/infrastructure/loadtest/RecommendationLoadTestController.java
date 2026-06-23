package com.wanted.codebombalms.recommendation.infrastructure.loadtest;

import com.wanted.codebombalms.recommendation.application.usecase.GenerateProblemSetRecommendationsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@Profile("loadtest")
@RequiredArgsConstructor
@RequestMapping("/internal/loadtest/recommendations")
public class RecommendationLoadTestController {

    private final GenerateProblemSetRecommendationsUseCase generateUseCase;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateProblemSetRecommendations() {
        long startedAt = System.nanoTime();

        int generatedUserCount = generateUseCase.generate();

        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("event=loadtest_recommendation_generation_triggered generatedUserCount={} durationMs={}",
                generatedUserCount, durationMs);

        return ResponseEntity.ok(Map.of(
                "status", "completed",
                "generatedUserCount", generatedUserCount,
                "durationMs", durationMs
        ));
    }
}
