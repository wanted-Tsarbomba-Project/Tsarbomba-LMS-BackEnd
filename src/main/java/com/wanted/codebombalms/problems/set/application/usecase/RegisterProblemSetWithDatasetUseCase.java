package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;
import com.wanted.codebombalms.problems.set.domain.model.CreatedProblemSummary;

import java.util.List;

public interface RegisterProblemSetWithDatasetUseCase {

    ProblemSetWithDatasetCreateView handle(
            RegisterProblemSetCommand problemSetCommand,
            UploadProblemDatasetCommand datasetCommand
    );

    record ProblemSetWithDatasetCreateView(
            Long problemSetId,
            Long datasetId,
            String title,
            String categoryName,
            Integer totalProblemCount,
            Integer createdProblemCount,
            Integer createdTestCaseCount,
            List<CreatedProblemSummary> problems,
            String datasetFileName,
            String startCode
    ) {
    }
}
