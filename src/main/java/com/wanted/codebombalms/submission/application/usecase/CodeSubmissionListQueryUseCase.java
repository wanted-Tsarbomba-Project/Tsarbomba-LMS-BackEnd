package com.wanted.codebombalms.submission.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

public interface CodeSubmissionListQueryUseCase {

    CodeSubmissionPageView handle(Long problemId, int page, int size);

    record CodeSubmissionPageView(
            List<CodeSubmissionListItemView> submissions,
            PageInfoView pageInfo
    ) {
    }

    record CodeSubmissionListItemView(
            Long submissionId,
            Long problemId,
            Boolean correct,
            Integer passedTestCount,
            Integer totalTestCount,
            String executionStatus,
            LocalDateTime submittedAt
    ) {
    }

    record PageInfoView(
            int currentPage,
            int totalPage,
            long totalCount
    ) {
    }
}