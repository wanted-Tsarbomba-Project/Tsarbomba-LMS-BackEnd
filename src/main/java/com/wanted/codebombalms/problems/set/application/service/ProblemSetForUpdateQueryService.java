package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.set.application.port.LoadDatasetForUpdatePort;
import com.wanted.codebombalms.problems.set.application.port.LoadHintForUpdatePort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetForUpdateBasePort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemsForUpdatePort;
import com.wanted.codebombalms.problems.set.application.port.LoadTestCasesForUpdatePort;
import com.wanted.codebombalms.problems.set.application.query.GetProblemSetForUpdateQuery;
import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetForUpdateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemSetForUpdateQueryService implements GetProblemSetForUpdateUseCase {

    private final LoadProblemSetForUpdateBasePort loadProblemSetForUpdateBasePort;
    private final LoadProblemsForUpdatePort loadProblemsForUpdatePort;
    private final LoadHintForUpdatePort loadHintForUpdatePort;
    private final LoadDatasetForUpdatePort loadDatasetForUpdatePort;
    private final LoadTestCasesForUpdatePort loadTestCasesForUpdatePort;


    @Override
    public ProblemSetForUpdateView handle(GetProblemSetForUpdateQuery query) {
        var problemSet = loadProblemSetForUpdateBasePort.loadBase(query.problemSetId());
        var dataset = loadDatasetForUpdatePort.loadActiveDatasetForUpdate(query.problemSetId());

        var problems = loadProblemsForUpdatePort.loadProblemsForUpdate(query.problemSetId())
                .stream()
                .map(problem -> {
                    var hint = loadHintForUpdatePort.loadFirstHintForUpdate(problem.problemId());
                    var testCases = loadTestCasesForUpdatePort.loadActiveTestCasesForUpdate(problem.problemId())
                            .stream()
                            .map(testCase -> new TestCaseForUpdateView(
                                    testCase.testCaseId(),
                                    testCase.testCode(),
                                    testCase.hidden(),
                                    testCase.timeoutMs()
                            ))
                            .toList();

                    return new ProblemForUpdateView(
                            problem.problemId(),
                            problem.title(),
                            problem.content(),
                            problem.point(),
                            createStartCode(dataset.map(LoadDatasetForUpdatePort.DatasetForUpdateData::fileUrl).orElse(null)),
                            hint.map(LoadHintForUpdatePort.HintForUpdateData::hintId).orElse(null),
                            hint.map(LoadHintForUpdatePort.HintForUpdateData::hintContent).orElse(null),
                            problem.explanation(),
                            testCases
                    );
                })
                .toList();

        return new ProblemSetForUpdateView(
                problemSet.problemSetId(),
                problemSet.title(),
                problemSet.categoryName(),
                problemSet.difficulty(),
                problemSet.description(),
                dataset.map(LoadDatasetForUpdatePort.DatasetForUpdateData::originalFileName).orElse(null),
                dataset.map(LoadDatasetForUpdatePort.DatasetForUpdateData::datasetId).orElse(null),
                dataset.map(LoadDatasetForUpdatePort.DatasetForUpdateData::fileUrl).orElse(null),
                problems
        );
    }

    private String createStartCode(String datasetUrl) {
        if (datasetUrl == null || datasetUrl.isBlank()) {
            return null;
        }

        return "import pandas as pd\n\n"
                + "df = pd.read_csv(\"" + datasetUrl + "\")";
    }
}
