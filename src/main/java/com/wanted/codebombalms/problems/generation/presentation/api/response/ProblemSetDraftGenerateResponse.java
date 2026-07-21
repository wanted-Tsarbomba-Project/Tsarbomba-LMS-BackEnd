package com.wanted.codebombalms.problems.generation.presentation.api.response;

import com.wanted.codebombalms.problems.generation.application.result.ProblemSetDraftResult;
import com.wanted.codebombalms.problems.generation.domain.GeneratedProblemDraft;
import com.wanted.codebombalms.problems.generation.domain.GeneratedTestCaseDraft;
import com.wanted.codebombalms.problems.generation.domain.ProblemSetDraft;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI 문제세트 초안 생성 응답")
public record ProblemSetDraftGenerateResponse(
        @Schema(description = "AI 생성 결과 요약")
        String answer,

        @Schema(description = "문제세트 초안")
        DraftResponse draft,

        @Schema(description = "AI 서버에서 사용한 도구 목록")
        List<String> usedTools
) {

    public static ProblemSetDraftGenerateResponse from(ProblemSetDraftResult result) {
        return new ProblemSetDraftGenerateResponse(
                result.answer(),
                DraftResponse.from(result.draft()),
                result.usedTools()
        );
    }

    public record DraftResponse(
            String title,
            String categoryName,
            String difficulty,
            String description,
            String dataFileName,
            List<ProblemResponse> problems
    ) {

        public static DraftResponse from(ProblemSetDraft draft) {
            if (draft == null) {
                return null;
            }

            return new DraftResponse(
                    draft.title(),
                    draft.categoryName(),
                    draft.difficulty(),
                    draft.description(),
                    draft.dataFileName(),
                    draft.problems().stream()
                            .map(ProblemResponse::from)
                            .toList()
            );
        }
    }

    public record ProblemResponse(
            String title,
            int point,
            String content,
            String startCode,
            String hint,
            String explanation,
            List<TestCaseResponse> testCases
    ) {

        public static ProblemResponse from(GeneratedProblemDraft problem) {
            return new ProblemResponse(
                    problem.title(),
                    problem.point(),
                    problem.content(),
                    problem.startCode(),
                    problem.hint(),
                    problem.explanation(),
                    problem.testCases().stream()
                            .map(TestCaseResponse::from)
                            .toList()
            );
        }
    }

    public record TestCaseResponse(
            String testCode,
            boolean isHidden,
            int timeoutMs
    ) {

        public static TestCaseResponse from(GeneratedTestCaseDraft testCase) {
            return new TestCaseResponse(
                    testCase.testCode(),
                    testCase.hidden(),
                    testCase.timeoutMs()
            );
        }
    }
}
