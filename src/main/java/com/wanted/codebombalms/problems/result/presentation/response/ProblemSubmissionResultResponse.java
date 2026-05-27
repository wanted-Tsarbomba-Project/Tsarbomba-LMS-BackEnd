package com.wanted.codebombalms.problems.result.presentation.response;

import com.wanted.codebombalms.problems.result.application.usecase.GetProblemSetResultUseCase.ProblemSubmissionResultView;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record ProblemSubmissionResultResponse(
        @Schema(description = "문제 ID", example = "3001")
        Long problemId,

        @Schema(description = "문제 세트 내 문제 번호", example = "1")
        Integer problemNumber,

        @Schema(description = "문제 제목", example = "데이터 행과 열 개수 확인")
        String title,

        @Schema(description = "문제 내용", example = "employee_performance.csv 파일을 불러온 뒤 DataFrame의 행과 열 개수를 확인하세요.")
        String content,

        @Schema(description = "학생이 제출한 텍스트 답안. 코드 실행형 문제에서는 null일 수 있습니다.", example = "재방문율", nullable = true)
        String submittedAnswer,

        @Schema(description = "정답 여부", example = "true")
        Boolean isCorrect,

        @Schema(description = "최종 제출 시각", example = "2026-05-27T11:30:00")
        LocalDateTime submittedAt,

        @Schema(description = "문제 해설. 오답 또는 완료 결과 화면에서 표시할 수 있습니다.", example = "DataFrame의 shape 속성을 사용하면 행과 열 개수를 튜플로 확인할 수 있습니다.")
        String explanation
) {
    public ProblemSubmissionResultResponse(ProblemSubmissionResultView submission) {
        this(
                submission.problemId(),
                submission.problemNumber(),
                submission.title(),
                submission.content(),
                submission.submittedAnswer(),
                submission.isCorrect(),
                submission.submittedAt(),
                submission.explanation()
        );
    }
}
