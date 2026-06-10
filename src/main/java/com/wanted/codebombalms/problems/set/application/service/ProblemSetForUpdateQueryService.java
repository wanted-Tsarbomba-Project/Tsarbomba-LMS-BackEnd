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

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        var problemData = loadProblemsForUpdatePort.loadProblemsForUpdate(query.problemSetId());

        var problemIds = problemData.stream()
                .map(LoadProblemsForUpdatePort.ProblemForUpdateData::problemId)
                .toList();
        var testCasesByProblemId =
                loadTestCasesForUpdatePort.loadActiveTestCasesForUpdate(problemIds);
        var hintsByProblemId =
                loadHintForUpdatePort.loadFirstHintsForUpdate(problemIds);

        var problems = problemData.stream()
                .map(problem -> toProblemView(
                        problem,
                        dataset,
                        testCasesByProblemId,
                        hintsByProblemId
                ))
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

    private ProblemForUpdateView toProblemView(
            LoadProblemsForUpdatePort.ProblemForUpdateData problem,
            Optional<LoadDatasetForUpdatePort.DatasetForUpdateData> dataset,
            Map<Long, List<LoadTestCasesForUpdatePort.TestCaseForUpdateData>> testCasesByProblemId,
            Map<Long, LoadHintForUpdatePort.HintForUpdateData> hintsByProblemId
    ) {
        var hint = Optional.ofNullable(
                hintsByProblemId.get(problem.problemId())
        );
        var testCases = toTestCaseViews(testCasesByProblemId.getOrDefault(
                problem.problemId(),
                List.of()
        ));

        return new ProblemForUpdateView(
                problem.problemId(),
                problem.title(),
                problem.content(),
                problem.point(),
                createStartCode(dataset.map(
                        LoadDatasetForUpdatePort.DatasetForUpdateData::fileUrl
                ).orElse(null)),
                hint.map(LoadHintForUpdatePort.HintForUpdateData::hintId).orElse(null),
                hint.map(LoadHintForUpdatePort.HintForUpdateData::hintContent).orElse(null),
                problem.explanation(),
                testCases
        );
    }

    private List<TestCaseForUpdateView> toTestCaseViews(
            List<LoadTestCasesForUpdatePort.TestCaseForUpdateData> testCases
    ) {
        return testCases.stream()
                .map(testCase -> new TestCaseForUpdateView(
                        testCase.testCaseId(),
                        testCase.testCode(),
                        testCase.hidden(),
                        testCase.timeoutMs()
                ))
                .toList();
    }

    private String createStartCode(String datasetUrl) {
        if (datasetUrl == null || datasetUrl.isBlank()) {
            return null;
        }

        return "import pandas as pd\n\n"
                + "df = pd.read_csv(\"" + datasetUrl + "\")";
    }
}
