package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.dataset.application.port.LoadActiveDatasetUrlPort;
import com.wanted.codebombalms.problems.problem.application.port.LoadProblemSetIdByProblemIdPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemStartCodePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProblemStartCodeService implements LoadProblemStartCodePort {

    private final LoadProblemSetIdByProblemIdPort loadProblemSetIdByProblemIdPort;
    private final LoadActiveDatasetUrlPort loadActiveDatasetUrlPort;

    @Override
    public String loadStartCode(Long problemId) {
        Long problemSetId = loadProblemSetIdByProblemIdPort.loadProblemSetIdByProblemId(problemId);
        String datasetUrl = loadActiveDatasetUrlPort.loadActiveDatasetUrl(problemSetId);

        if (datasetUrl == null || datasetUrl.isBlank()) {
            return null;
        }

        return "import pandas as pd\n\n"
                + "df = pd.read_csv(\"" + datasetUrl + "\")";
    }
}
