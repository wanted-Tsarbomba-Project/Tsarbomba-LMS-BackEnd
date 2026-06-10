package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.dataset.application.port.GenerateDatasetAccessUrlPort;
import com.wanted.codebombalms.problems.dataset.application.port.LoadActiveDatasetFilePathPort;
import com.wanted.codebombalms.problems.problem.application.port.LoadProblemSetIdByProblemIdPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemStartCodePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProblemStartCodeService implements LoadProblemStartCodePort {

    private final LoadProblemSetIdByProblemIdPort loadProblemSetIdByProblemIdPort;
    private final LoadActiveDatasetFilePathPort loadActiveDatasetFilePathPort;
    private final GenerateDatasetAccessUrlPort generateDatasetAccessUrlPort;

    @Override
    public String loadStartCode(Long problemId) {
        Long problemSetId = loadProblemSetIdByProblemIdPort.loadProblemSetIdByProblemId(problemId);
        String filePath = loadActiveDatasetFilePathPort.loadActiveDatasetFilePath(problemSetId);

        if (filePath == null || filePath.isBlank()) {
            return null;
        }

        String datasetAccessUrl = generateDatasetAccessUrlPort.generate(filePath);

        return "import pandas as pd\n\n"
                + "df = pd.read_csv(\"" + datasetAccessUrl + "\")";
    }
}
