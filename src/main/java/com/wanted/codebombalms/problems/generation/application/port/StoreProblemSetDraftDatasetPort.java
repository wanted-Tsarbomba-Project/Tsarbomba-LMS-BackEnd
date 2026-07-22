package com.wanted.codebombalms.problems.generation.application.port;

import com.wanted.codebombalms.problems.generation.application.command.StoreProblemSetDraftDatasetCommand;
import com.wanted.codebombalms.problems.generation.domain.StoredProblemSetDraftDataset;

public interface StoreProblemSetDraftDatasetPort {

    StoredProblemSetDraftDataset store(StoreProblemSetDraftDatasetCommand command);
}
