package com.wanted.codebombalms.problems.dataset.presentation.api.response;

import com.wanted.codebombalms.problems.dataset.application.usecase.IssueDatasetDownloadUrlUseCase.DatasetDownloadUrlResult;

public record DatasetDownloadUrlResponse(
        String fileName,
        String downloadUrl
) {
    public static DatasetDownloadUrlResponse from(
            DatasetDownloadUrlResult result
    ) {
        return new DatasetDownloadUrlResponse(
                result.fileName(),
                result.downloadUrl()
        );
    }
}
