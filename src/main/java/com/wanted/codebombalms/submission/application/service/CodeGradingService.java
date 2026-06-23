package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.problems.execution.application.port.RunCodePort;
import com.wanted.codebombalms.submission.application.port.LoadTestCasesForGradingPort.TestCaseForGrading;
import com.wanted.codebombalms.submission.domain.model.SubmissionTestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeGradingService {

    private static final String SUCCESS = "SUCCESS";
    private static final String WRONG_ANSWER = "WRONG_ANSWER";
    private static final String GRADING_FAILED = "GRADING_FAILED";

    private final RunCodePort runCodePort;

    public CodeGradingResult grade(String code, String datasetAccessUrl,List<TestCaseForGrading> testCases) {
        if (testCases == null || testCases.isEmpty()) {
            return new CodeGradingResult(
                    false,
                    0,
                    0,
                    GRADING_FAILED,
                    "채점 기준 테스트케이스가 없습니다.",
                    List.of()
            );
        }

        List<SubmissionTestResult> testResults = new ArrayList<>();
        int passedCount = 0;

        for (TestCaseForGrading testCase : testCases) {
            String gradingCode = buildGradingCode(code, testCase.testCode());

            RunCodePort.CodeRunResult runResult = runCodePort.run(new RunCodePort.CodeRunCommand(
                    gradingCode,
                    datasetAccessUrl,
                    testCase.timeoutMs()
            ));
            boolean passed = Boolean.TRUE.equals(runResult.success());

            if (passed) {
                passedCount++;
            }

            testResults.add(new SubmissionTestResult(
                    testCase.testCaseId(),
                    passed,
                    runResult.output(),
                    runResult.errorMessage(),
                    toIntegerExecutionTime(runResult.executionTimeMs())
            ));
        }

        int totalCount = testCases.size();
        boolean correct = passedCount == totalCount;

        return new CodeGradingResult(
                correct,
                passedCount,
                totalCount,
                correct ? SUCCESS : WRONG_ANSWER,
                correct ? null : (totalCount - passedCount) + "개의 테스트케이스를 통과하지 못했습니다.",
                testResults
        );
    }

    private String buildGradingCode(String userCode, String testCode) {
        return userCode
                + System.lineSeparator()
                + System.lineSeparator()
                + testCode;
    }

    private Integer toIntegerExecutionTime(Long executionTimeMs) {
        if (executionTimeMs == null) {
            return null;
        }

        if (executionTimeMs > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return executionTimeMs.intValue();
    }

    public record CodeGradingResult(
            Boolean correct,
            Integer passedTestCount,
            Integer totalTestCount,
            String executionStatus,
            String errorMessage,
            List<SubmissionTestResult> testResults
    ) {
    }
}
