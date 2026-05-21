package com.wanted.codebombalms.problems.dataset.application.port;

import com.wanted.codebombalms.problems.dataset.application.command.ConnectProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDataset;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnection;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;

public interface ProblemDatasetManagementPort {

    ProblemDatasetConnection connectDataset(ConnectProblemDatasetCommand command);

    ProblemDataset saveUploadedDataset(StoredDatasetFile storedFile);
}
