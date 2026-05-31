package com.wanted.codebombalms.user.presentation.api.response;

import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "학생 문제 풀이 제출 기록 목록 응답")
public record StudentProblemSubmissionListResponse(
        @Schema(description = "학생 회원 ID", example = "3")
        Long userId,

        @Schema(description = "조회된 제출 기록 수", example = "4")
        Integer totalCount,

        @Schema(description = "학생 문제 제출 기록 목록")
        List<StudentProblemSubmissionResponse> submissions
) {
    public static StudentProblemSubmissionListResponse from(StudentProblemSubmissionResult result) {
        return new StudentProblemSubmissionListResponse(
                result.userId(),
                result.totalCount(),
                result.submissions().stream()
                        .map(StudentProblemSubmissionResponse::from)
                        .toList()
        );
    }
}
