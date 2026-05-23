package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.submission.application.port.LoadTestCasesForGradingPort.TestCaseForGrading;
import com.wanted.codebombalms.submission.domain.model.SubmissionTestResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeGradingService {

    private static final String SUCCESS = "SUCCESS";
    private static final String WRONG_ANSWER = "WRONG_ANSWER";
    private static final String GRADING_FAILED = "GRADING_FAILED";
    private static final int MOCK_EXECUTION_TIME_MS = 1;

    public CodeGradingResult grade(String code, List<TestCaseForGrading> testCases) {
        if (testCases == null || testCases.isEmpty()) {
            return new CodeGradingResult(
                    false,
                    0,
                    0,
                    0,
                    GRADING_FAILED,
                    "채점 기준 테스트케이스가 없습니다.",
                    List.of()
            );
        }

        if (!code.contains("result")) {
            List<SubmissionTestResult> failedResults = testCases.stream()
                    .map(testCase -> new SubmissionTestResult(
                            testCase.testCaseId(),
                            false,
                            null,
                            "result 변수가 정의되지 않았습니다.",
                            MOCK_EXECUTION_TIME_MS,
                            0
                    ))
                    .toList();

            return new CodeGradingResult(
                    false,
                    0,
                    testCases.size(),
                    0,
                    WRONG_ANSWER,
                    "result 변수가 정의되지 않았습니다.",
                    failedResults
            );
        }

        int earnedScore = testCases.stream()
                .mapToInt(testCase -> testCase.score() == null ? 0 : testCase.score())
                .sum();
        List<SubmissionTestResult> passedResults = testCases.stream()
                .map(testCase -> new SubmissionTestResult(
                        testCase.testCaseId(),
                        true,
                        testCase.expectedResult(),
                        null,
                        MOCK_EXECUTION_TIME_MS,
                        testCase.score() == null ? 0 : testCase.score()
                ))
                .toList();

        return new CodeGradingResult(
                true,
                testCases.size(),
                testCases.size(),
                earnedScore,
                SUCCESS,
                null,
                passedResults
        );
    }

    public record CodeGradingResult(
            Boolean correct,
            Integer passedTestCount,
            Integer totalTestCount,
            Integer earnedScore,
            String executionStatus,
            String errorMessage,
            List<SubmissionTestResult> testResults
    ) {
    }
}
