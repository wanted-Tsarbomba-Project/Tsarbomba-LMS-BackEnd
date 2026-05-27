package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionListQueryUseCase.PageInfoView;
import io.swagger.v3.oas.annotations.media.Schema;

public record PageInfoResponse(
        @Schema(description = "현재 페이지 번호", example = "1")
        int currentPage,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPage,

        @Schema(description = "전체 제출 기록 수", example = "42")
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
