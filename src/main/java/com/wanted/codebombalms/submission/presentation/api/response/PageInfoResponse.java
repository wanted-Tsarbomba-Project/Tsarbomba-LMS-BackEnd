package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionListQueryUseCase.PageInfoView;

public record PageInfoResponse(
        int currentPage,
        int totalPage,
        long totalCount
) {

    public PageInfoResponse(PageInfoView pageInfo) {
        this(
                pageInfo.currentPage(),
                pageInfo.totalPage(),
                pageInfo.totalCount()
        );
    }
}
