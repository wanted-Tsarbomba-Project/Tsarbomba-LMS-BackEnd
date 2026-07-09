package com.wanted.codebombalms.problems.explanation.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.problems.explanation.application.usecase.ViewProblemExplanationUseCase;
import com.wanted.codebombalms.problems.explanation.presentation.api.response.ExplanationViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProblemExplanationController {

    private final ViewProblemExplanationUseCase viewProblemExplanationUseCase;

    @PostMapping("/api/v1/problems/{problemId}/explanation-view")
    public ResponseEntity<ApiResponse<ExplanationViewResponse>> viewExplanation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long problemId
    ) {
        var result = viewProblemExplanationUseCase.viewExplanation(userId, problemId);

        return ResponseEntity.ok(ApiResponse.success(
                "COMMON-SUCCESS",
                "문제 해설 조회에 성공했습니다.",
                ExplanationViewResponse.from(result)
        ));
    }
}
