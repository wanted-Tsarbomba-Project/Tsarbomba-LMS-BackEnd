package com.wanted.codebombalms.problems.dataset.application.usecase;

public interface IssueDatasetDownloadUrlUseCase {

    DatasetDownloadUrlResult issueDownloadUrl(Long problemSetId);

    record DatasetDownloadUrlResult(
            String fileName,
            String downloadUrl
    ) {
    }
}
