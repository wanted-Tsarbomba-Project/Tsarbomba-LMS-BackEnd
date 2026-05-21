package com.wanted.codebombalms.problems.hint.presentation.api;

import com.wanted.codebombalms.problems.hint.application.usecase.FindProblemHintsUseCase;
import com.wanted.codebombalms.problems.hint.presentation.api.response.ProblemHintResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProblemHintController {

    private final FindProblemHintsUseCase findProblemHintsUseCase;

    public ProblemHintController(FindProblemHintsUseCase findProblemHintsUseCase) {
        this.findProblemHintsUseCase = findProblemHintsUseCase;
    }

    @GetMapping("/api/v1/problems/{problemId}/hints")
    public ResponseEntity<ApiResponse<List<ProblemHintResponse>>> findHints(@PathVariable Long problemId) {
        List<ProblemHintResponse> response = findProblemHintsUseCase.handle(problemId)
                .stream()
                .map(ProblemHintResponse::new)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }
}
