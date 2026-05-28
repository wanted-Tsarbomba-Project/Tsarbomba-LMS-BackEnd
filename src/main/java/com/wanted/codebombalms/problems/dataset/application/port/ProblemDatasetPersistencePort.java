package com.wanted.codebombalms.problems.dataset.application.port;

import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;

public interface ProblemDatasetPersistencePort {

    ProblemDataset saveUploadedDataset(Long problemSetId, StoredDatasetFile storedFile);

    ProblemDataset saveUploadedDataset(StoredDatasetFile storedFile);

}