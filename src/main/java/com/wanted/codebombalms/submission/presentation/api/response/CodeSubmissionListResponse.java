package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionListQueryUseCase.CodeSubmissionPageView;

import java.util.List;

public record CodeSubmissionListResponse(
        List<CodeSubmissionListItemResponse> submissions,
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
