package com.wanted.codebombalms.problems.dataset.application.port;

import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.dataset.domain.model.StoredDatasetFile;

import java.io.IOException;

public interface StoreDatasetFilePort {

    StoredDatasetFile store(UploadProblemDatasetCommand command) throws IOException;
}
