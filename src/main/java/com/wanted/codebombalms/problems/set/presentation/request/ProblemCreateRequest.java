package com.wanted.codebombalms.problems.set.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemCreateRequest(
        @Schema(description = "소문제 제목", example = "데이터 행과 열 개수 확인", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = "소문제 내용", example = "employee_performance.csv 파일을 불러온 뒤 DataFrame의 행과 열 개수를 확인하세요.", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,

        @Schema(description = "정답 시 지급할 포인트", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer point,

        @Schema(description = "코드 에디터에 기본으로 보여줄 시작 코드", nullable = true)
        String startCode,

        @Schema(description = "텍스트 문제 정답. 코드 실행형 문제에서는 null로 보낼 수 있습니다.", example = "정답 문자열", nullable = true)
        String answer,

        @Schema(description = "소문제 힌트", example = "DataFrame의 shape 속성을 사용해보세요.", nullable = true)
        String hint,

        @Schema(description = "정답 또는 풀이 해설", example = "df.shape는 행 개수와 열 개수를 튜플로 반환합니다.", nullable = true)
        String explanation
) {
}