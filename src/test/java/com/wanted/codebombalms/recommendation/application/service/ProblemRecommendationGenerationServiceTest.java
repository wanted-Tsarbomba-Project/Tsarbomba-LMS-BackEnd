package com.wanted.codebombalms.recommendation.application.service;

import com.wanted.codebombalms.recommendation.application.command.GeneratedProblemSetRecommendation;
import com.wanted.codebombalms.recommendation.application.command.GeneratedUserProblemSetRecommendations;
import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationCommandPort;
import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationGenerationClient;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationAlgorithm;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** Python 추천 생성 결과 검증과 저장 위임 정책을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ProblemRecommendationGenerationServiceTest {

    @Mock
    private ProblemRecommendationGenerationClient generationClient;

    @Mock
    private ProblemRecommendationCommandPort commandPort;

    @InjectMocks
    private ProblemRecommendationGenerationService service;

    /** 사용자별 추천 3개가 반환되면 저장 port에 교체 저장을 위임합니다. */
    @Test
    void generate_whenEachUserHasThreeRecommendations_replacesActiveRecommendations() {
        var recommendations = userRecommendations(1L, 3);
        given(generationClient.generateProblemSetRecommendations()).willReturn(List.of(recommendations));

        int generatedUserCount = service.generate();

        assertEquals(1, generatedUserCount);
        verify(commandPort).replaceActiveRecommendations(recommendations);
    }

    /** 추천 대상 사용자의 추천 개수가 3개가 아니면 저장하지 않고 예외를 발생시킵니다. */
    @Test
    void generate_whenRecommendationCountIsNotThree_throwsExceptionWithoutSaving() {
        var recommendations = userRecommendations(1L, 2);
        given(generationClient.generateProblemSetRecommendations()).willReturn(List.of(recommendations));

        assertThrows(IllegalStateException.class, () -> service.generate());

        verify(commandPort, never()).replaceActiveRecommendations(recommendations);
    }

    /** 테스트용 사용자별 추천 묶음을 생성합니다. */
    private GeneratedUserProblemSetRecommendations userRecommendations(Long userId, int count) {
        return new GeneratedUserProblemSetRecommendations(
                userId,
                java.util.stream.IntStream.rangeClosed(1, count)
                        .mapToObj(rankNo -> recommendation(100L + rankNo, rankNo))
                        .toList()
        );
    }

    /** 테스트용 문제 세트 추천 한 건을 생성합니다. */
    private GeneratedProblemSetRecommendation recommendation(Long problemSetId, int rankNo) {
        return new GeneratedProblemSetRecommendation(
                problemSetId,
                BigDecimal.valueOf(0.03),
                BigDecimal.valueOf(0.7),
                BigDecimal.valueOf(1.5),
                rankNo,
                RecommendationAlgorithm.APRIORI
        );
    }
}
