package com.wanted.codebombalms.problems.dataset.application.port;

import java.util.Optional;

public interface LoadDatasetDownloadInfoPort {

    Optional<DatasetDownloadInfo> loadActiveDataset(Long problemSetId);

    record DatasetDownloadInfo(
            String originalFileName,
            String filePath
    ) {
    }
}
