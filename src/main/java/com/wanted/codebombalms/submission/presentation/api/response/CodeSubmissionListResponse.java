package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionListQueryUseCase.CodeSubmissionPageView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CodeSubmissionListResponse(
        @Schema(description = "제출 기록 목록")
        List<CodeSubmissionListItemResponse> submissions,

        @Schema(description = "페이지 정보")
        PageInfoResponse pageInfo
) {

    public CodeSubmissionListResponse(CodeSubmissionPageView result) {
        this(
                result.submissions()
                        .stream()
                        .map(CodeSubmissionListItemResponse::new)
                        .toList(),
                new PageInfoResponse(result.pageInfo())
        );
    }
}
