package com.wanted.codebombalms.recommendation.presentation;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.recommendation.application.usecase.GetMyProblemSetRecommendationsUseCase;
import com.wanted.codebombalms.recommendation.application.usecase.HideProblemSetRecommendationsTodayUseCase;
import com.wanted.codebombalms.recommendation.presentation.response.ProblemSetRecommendationListResponse;
import com.wanted.codebombalms.recommendation.presentation.response.RecommendationHideTodayResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 문제 세트 추천 목록 조회와 오늘 하루 숨김 API를 제공합니다. */
@Tag(name = "Recommendation - 문제 세트 추천", description = "로그인 학생의 문제 세트 추천 목록 조회/숨김 API")
@RestController
@RequiredArgsConstructor
public class ProblemRecommendationController {

    private final GetMyProblemSetRecommendationsUseCase getMyProblemSetRecommendationsUseCase;
    private final HideProblemSetRecommendationsTodayUseCase hideProblemSetRecommendationsTodayUseCase;

    /** 로그인 사용자의 추천 문제 세트 목록을 조회합니다. */
    @Operation(
            summary = "내 문제 세트 추천 목록 조회",
            description = "로그인 학생의 ACTIVE 문제 세트 추천 목록을 rankNo 오름차순으로 조회합니다. "
                    + "오늘 하루 숨김 상태이면 추천 DB를 조회하지 않고 빈 목록을 반환합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추천 목록 조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다")
    @GetMapping("/api/v1/recommendations/problem-sets/me")
    public ResponseEntity<ApiResponse<ProblemSetRecommendationListResponse>> findMyProblemSetRecommendations(
            @AuthenticationPrincipal Long userId,

            @Parameter(description = "조회할 추천 개수. 최대 3개로 제한됩니다.", example = "3")
            @RequestParam(defaultValue = "3") Integer limit
    ) {
        validateAuthenticated(userId);

        var result = getMyProblemSetRecommendationsUseCase.getRecommendations(userId, limit);

        return ResponseEntity.ok(ApiResponse.success(
                RecommendationResponseCode.PROBLEM_SET_RECOMMENDATIONS_RETRIEVED,
                RecommendationResponseMessage.PROBLEM_SET_RECOMMENDATIONS_RETRIEVED,
                ProblemSetRecommendationListResponse.from(result)
        ));
    }

    /** 로그인 사용자의 추천 문제 세트를 오늘 하루 숨김 처리합니다. */
    @Operation(
            summary = "내 문제 세트 추천 오늘 하루 숨김",
            description = "로그인 학생의 문제 세트 추천 영역을 오늘 23:59:59까지 숨김 처리합니다. 추천 row는 유지합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추천 숨김 처리 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다")
    @PostMapping("/api/v1/recommendations/problem-sets/hide-today")
    public ResponseEntity<ApiResponse<RecommendationHideTodayResponse>> hideMyProblemSetRecommendationsToday(
            @AuthenticationPrincipal Long userId
    ) {
        validateAuthenticated(userId);

        var result = hideProblemSetRecommendationsTodayUseCase.hideToday(userId);

        return ResponseEntity.ok(ApiResponse.success(
                RecommendationResponseCode.PROBLEM_SET_RECOMMENDATIONS_HIDDEN,
                RecommendationResponseMessage.PROBLEM_SET_RECOMMENDATIONS_HIDDEN,
                RecommendationHideTodayResponse.from(result)
        ));
    }

    /** 인증 principal이 없으면 공통 인증 예외를 발생시킵니다. */
    private void validateAuthenticated(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_REQUIRED);
        }
    }
}
