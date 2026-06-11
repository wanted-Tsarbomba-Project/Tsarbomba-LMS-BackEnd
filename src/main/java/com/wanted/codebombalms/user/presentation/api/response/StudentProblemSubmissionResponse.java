package com.wanted.codebombalms.user.presentation.api.response;

import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "학생 문제 제출 기록 응답")
public record StudentProblemSubmissionResponse(
        @Schema(description = "문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "문제 세트명", example = "pandas 기초 분석 문제 세트")
        String problemSetTitle,

        @Schema(description = "문제 세트 설명", example = "CSV 데이터를 불러와 기본 정보를 확인하는 코드 실행형 문제 세트입니다.")
        String problemSetDescription,

        @Schema(description = "문제 세트 난이도", example = "EASY")
        String problemSetDifficulty,

        @Schema(description = "문제 세트 전체 문제 수", example = "2")
        Integer totalProblemCount,

        @Schema(description = "문제 ID", example = "3001")
        Long problemId,

        @Schema(description = "문제 제목", example = "데이터 행과 열 개수 확인")
        String problemTitle,

        @Schema(description = "문제 유형", example = "CODE")
        String problemType,

        @Schema(description = "문제 난이도", example = "EASY")
        String problemDifficulty,

        @Schema(description = "문제 세트 내 문제 순서", example = "1")
        Integer problemOrder,

        @Schema(description = "문제 정답 시 지급 포인트", example = "10")
        Integer point,

        @Schema(description = "제출 시도 제한 횟수", example = "3")
        Integer attemptLimit,

        @Schema(description = "재시도 가능 여부", example = "true")
        Boolean isRetriable,

        @Schema(description = "제출 ID", example = "5001")
        Long submissionId,

        @Schema(description = "사용자가 제출한 코드", example = "result = df.shape", nullable = true)
        String submittedCode,

        @Schema(description = "정답 여부", example = "true")
        Boolean isCorrect,

        @Schema(description = "해당 제출로 획득한 포인트", example = "10")
        Integer earnedPoint,

        @Schema(description = "제출 회차", example = "1")
        Integer attemptNo,

        @Schema(description = "제출 일시", example = "2026-05-28T20:30:00")
        LocalDateTime submittedAt,

        @Schema(description = "제출 상태", example = "CORRECT", allowableValues = {"CORRECT", "INCORRECT", "SUBMITTED"})
        String submissionStatus
) {
    public static StudentProblemSubmissionResponse from(StudentProblemSubmissionItem item) {
        return new StudentProblemSubmissionResponse(
                item.problemSetId(),
                item.problemSetTitle(),
                item.problemSetDescription(),
                item.problemSetDifficulty(),
                item.totalProblemCount(),

                item.problemId(),
                item.problemTitle(),
                item.problemType(),
                item.problemDifficulty(),
                item.problemOrder(),
                item.point(),
                item.attemptLimit(),
                item.retriable(),

                item.submissionId(),
                item.submittedCode(),
                item.correct(),
                item.earnedPoint(),
                item.attemptNo(),
                item.submittedAt(),
                item.submissionStatus()
        );
    }
}
