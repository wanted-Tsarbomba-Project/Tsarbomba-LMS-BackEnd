package com.wanted.codebombalms.problems.dataset.domain.repository;

import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnection;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnectionRequest;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnectionTarget;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;

public interface ProblemDatasetRepository {

    ProblemDatasetConnectionTarget loadConnectionTarget(ProblemDatasetConnectionRequest request);

    ProblemDatasetConnection connectDataset(ProblemDatasetConnectionRequest request);

    ProblemDataset saveUploadedDataset(StoredDatasetFile storedFile);
}
