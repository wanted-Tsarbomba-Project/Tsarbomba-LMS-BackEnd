package com.wanted.codebombalms.problems.dataset.application.usecase;

public interface IssueDatasetDownloadUrlUseCase {

    DatasetDownloadUrlResult issueDownloadUrl(Long userId,
                                              Long problemSetId);

    record DatasetDownloadUrlResult(
            String fileName,
            String downloadUrl
    ) {
    }
}
