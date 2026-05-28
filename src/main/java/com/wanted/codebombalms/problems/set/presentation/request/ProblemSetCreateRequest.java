package com.wanted.codebombalms.problems.set.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProblemSetCreateRequest(
        @Schema(description = "문제 세트 제목", example = "pandas 기초 분석 문제 세트", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = "문제 카테고리명입니다. 이미 등록된 카테고리를 사용합니다.", example = "Python 데이터 분석", requiredMode = Schema.RequiredMode.REQUIRED)
        String categoryName,

        @Schema(description = "문제 세트 설명", example = "CSV 데이터를 불러와 기본 정보를 확인하는 코드 실행형 문제 세트입니다.")
        String description,

        @Schema(description = "문제 세트 난이도", example = "EASY", allowableValues = {"EASY", "MEDIUM", "HARD"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String difficulty,

        @Schema(description = "참고용 데이터 파일명입니다. 실제 파일 업로드는 with-dataset API의 datasetFile 파트로 처리합니다.", example = "employee_performance.csv", nullable = true)
        String dataFileName,

        @Schema(description = "문제 세트에 포함할 소문제 목록입니다. 1개 이상 필요합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        List<ProblemCreateRequest> problems
) {
}