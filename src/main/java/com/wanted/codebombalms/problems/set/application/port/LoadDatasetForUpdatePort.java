package com.wanted.codebombalms.problems.set.application.port;

import java.util.Optional;

public interface LoadDatasetForUpdatePort {

    Optional<DatasetForUpdateData> loadActiveDatasetForUpdate(Long problemSetId);

    record DatasetForUpdateData(
            Long datasetId,
            String originalFileName
    ) {
    }
}
