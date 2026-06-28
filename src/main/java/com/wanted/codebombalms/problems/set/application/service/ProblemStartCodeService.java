package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.dataset.application.port.LoadActiveDatasetFilePathPort;
import com.wanted.codebombalms.problems.problem.application.port.LoadProblemSetIdByProblemIdPort;
import com.wanted.codebombalms.problems.set.application.policy.DatasetStartCodePolicy;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemStartCodePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProblemStartCodeService implements LoadProblemStartCodePort {

    private final LoadProblemSetIdByProblemIdPort loadProblemSetIdByProblemIdPort;
    private final LoadActiveDatasetFilePathPort loadActiveDatasetFilePathPort;
    private final DatasetStartCodePolicy datasetStartCodePolicy;

    @Override
    public String loadStartCode(Long problemId) {
        Long problemSetId =
                loadProblemSetIdByProblemIdPort.loadProblemSetIdByProblemId(problemId);

        return loadStartCodeByProblemSetId(problemSetId);
    }

    @Override
    public String loadStartCodeByProblemSetId(Long problemSetId) {
        String datasetFilePath =
                loadActiveDatasetFilePathPort.loadActiveDatasetFilePath(problemSetId);

        return datasetStartCodePolicy.createIfDatasetExists(
                datasetFilePath != null && !datasetFilePath.isBlank()
        );
    }
}
