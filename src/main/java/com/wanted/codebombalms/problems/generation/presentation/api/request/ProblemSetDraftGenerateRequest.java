package com.wanted.codebombalms.problems.generation.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 문제세트 초안 생성 요청")
public record ProblemSetDraftGenerateRequest(
        @Schema(description = "관리자가 원하는 문제 생성 방향")
        @NotBlank(message = "문제 생성 요청 문구는 필수입니다.")
        @Size(max = 1_000, message = "문제 생성 요청 문구는 1000자 이하여야 합니다.")
        String question,

        @Schema(description = "데이터 파일명", example = "sw_engineer_salary.csv")
        @Size(max = 255, message = "데이터 파일명은 255자 이하여야 합니다.")
        String dataFileName,

        @Schema(description = "문제 주제", example = "SW기술자 평균임금 분석")
        @NotBlank(message = "문제 주제는 필수입니다.")
        @Size(max = 100, message = "문제 주제는 100자 이하여야 합니다.")
        String topic,

        @Schema(description = "카테고리 이름", example = "공공 데이터")
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다.")
        String categoryName,

        @Schema(description = "난이도", example = "EASY", allowableValues = {"EASY", "MEDIUM", "HARD"})
        @NotBlank(message = "난이도는 필수입니다.")
        @Pattern(regexp = "EASY|MEDIUM|HARD", message = "난이도는 EASY, MEDIUM, HARD만 가능합니다.")
        String difficulty,

        @Schema(description = "생성할 문제세트 개수", example = "1")
        @Min(1)
        @Max(1)
        int problemCount,

        @Schema(description = "생성할 소문제 개수", example = "3")
        @Min(1)
        @Max(10)
        int subProblemCount
) {
}
