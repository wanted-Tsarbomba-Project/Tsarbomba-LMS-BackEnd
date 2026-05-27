package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase.ProblemDetailView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemDetailResponse(
        @Schema(description = "문제 ID", example = "3001")
        Long problemId,

        @Schema(description = "문제 세트 내 소문제 번호", example = "1")
        Integer problemNumber,

        @Schema(description = "소문제 제목", example = "데이터 행과 열 개수 확인")
        String title,

        @Schema(description = "소문제 내용", example = "employee_performance.csv 파일을 불러온 뒤 DataFrame의 행과 열 개수를 확인하세요.")
        String content,

        @Schema(description = "문제 유형", example = "CODE")
        String problemType,

        @Schema(description = "정답 시 지급할 포인트", example = "10")
        Integer point,

        @Schema(
                description = "코드 에디터에 표시할 시작 코드. 데이터셋이 연결된 코드 문제는 GCS CSV URL을 읽는 코드가 포함될 수 있습니다.",
                example = """
                        import pandas as pd

                        df = pd.read_csv("https://storage.googleapis.com/codebombalms/problem_dataset/28679fde-6075-4c2e-a3f0-190fa3d80db7_employee_performance.csv")
                        """,
                nullable = true
        )
        String startCode
) {
    public ProblemDetailResponse(ProblemDetailView problem) {
        this(
                problem.problemId(),
                problem.problemNumber(),
                problem.title(),
                problem.content(),
                problem.problemType(),
                problem.point(),
                problem.startCode()
        );
    }
}
