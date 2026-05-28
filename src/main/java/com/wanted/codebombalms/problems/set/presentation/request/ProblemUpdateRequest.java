package com.wanted.codebombalms.problems.set.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemUpdateRequest(
        @Schema(description = "수정할 소문제 ID입니다. 기존 문제 수정 시 전달합니다.", example = "3001", nullable = true)
        Long problemId,

        @Schema(description = "소문제 제목", example = "데이터 행과 열 개수 확인", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = "소문제 내용", example = "employee_performance.csv 파일을 불러온 뒤 DataFrame의 행과 열 개수를 확인하세요.", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,

        @Schema(description = "정답 시 지급할 포인트입니다. 1 이상이어야 합니다.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer point,

        @Schema(
                description = "코드 에디터에 기본으로 표시할 시작 코드입니다.",
                example = """
                        import pandas as pd

                        df = pd.read_csv("https://storage.googleapis.com/codebombalms/problem_dataset/uuid_employee_performance.csv")
                        """,
                nullable = true
        )
        String startCode,

        @Schema(description = "텍스트 문제의 정답입니다. 코드 실행형 문제에서는 null로 보낼 수 있습니다.", example = "정답 문자열", nullable = true)
        String answer,

        @Schema(description = "수정할 힌트 ID", example = "3001", nullable = true)
        Long hintId,

        @Schema(description = "소문제 힌트", example = "DataFrame의 shape 속성을 사용해보세요.", nullable = true)
        String hint,

        @Schema(description = "풀이 또는 해설", example = "df.shape는 (행 개수, 열 개수) 튜플을 반환합니다.", nullable = true)
        String explanation
) {
}