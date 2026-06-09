package com.wanted.codebombalms.problems.set.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ProblemCreateRequest(
        @Schema(description = "문제 제목", example = "데이터 행과 열 개수 확인", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(description = "문제 내용", example = "CSV 파일을 불러온 뒤 DataFrame의 행과 열 개수를 확인하세요.", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,
        @Schema(description = "정답 시 지급할 포인트", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer point,
        @Schema(description = "코드 에디터에 기본으로 표시할 시작 코드", nullable = true)
        String startCode,
        @Schema(description = "문제 힌트", example = "DataFrame의 shape 속성을 사용해보세요.", nullable = true)
        String hint,
        @Schema(description = "정답 제출 후 제공할 해설", example = "df.shape는 행과 열 개수를 튜플로 반환합니다.", nullable = true)
        String explanation,
        @Schema(description = "문제 채점용 테스트 케이스 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        List<ProblemTestCaseCreateItemRequest> testCases
) {
}
