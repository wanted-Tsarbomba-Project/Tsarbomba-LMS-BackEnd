package com.wanted.codebombalms.problems.generation.infrastructure.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanted.codebombalms.problems.generation.application.result.ProblemSetDraftResult;
import com.wanted.codebombalms.problems.generation.domain.GeneratedProblemDraft;
import com.wanted.codebombalms.problems.generation.domain.GeneratedTestCaseDraft;
import com.wanted.codebombalms.problems.generation.domain.ProblemSetDraft;

import java.util.List;

public record FastApiProblemSetDraftResponse(
        String answer,
        DraftResponse draft,

        @JsonProperty("used_tools")
        List<String> usedTools
) {

    public ProblemSetDraftResult toResult() {
        return new ProblemSetDraftResult(
                answer,
                draft == null ? null : draft.toDomain(),
                usedTools == null ? List.of() : usedTools
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

        private ProblemSetDraft toDomain() {
            return new ProblemSetDraft(
                    title,
                    categoryName,
                    difficulty,
                    description,
                    dataFileName,
                    problems == null
                            ? List.of()
                            : problems.stream()
                            .map(ProblemResponse::toDomain)
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

        private GeneratedProblemDraft toDomain() {
            return new GeneratedProblemDraft(
                    title,
                    point,
                    content,
                    startCode,
                    hint,
                    explanation,
                    testCases == null
                            ? List.of()
                            : testCases.stream()
                            .map(TestCaseResponse::toDomain)
                            .toList()
            );
        }
    }

    public record TestCaseResponse(
            String testCode,

            @JsonProperty("isHidden")
            boolean hidden,

            int timeoutMs
    ) {

        private GeneratedTestCaseDraft toDomain() {
            return new GeneratedTestCaseDraft(
                    testCode,
                    hidden,
                    timeoutMs
            );
        }
    }
}
