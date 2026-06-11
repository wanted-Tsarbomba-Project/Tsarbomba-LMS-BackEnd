package com.wanted.codebombalms.learning.infrastructure.problem;

import com.wanted.codebombalms.learning.application.port.LearningProblemGradingPort;
import com.wanted.codebombalms.problems.dataset.application.port.GenerateDatasetAccessUrlPort;
import com.wanted.codebombalms.problems.dataset.application.port.LoadActiveDatasetFilePathPort;
import com.wanted.codebombalms.submission.application.port.LoadTestCasesForGradingPort;
import com.wanted.codebombalms.submission.application.policy.SubmissionCodePolicy;
import com.wanted.codebombalms.submission.application.service.CodeGradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LearningProblemGradingAdapter implements LearningProblemGradingPort {

    private final SubmissionCodePolicy submissionCodePolicy;
    private final LoadTestCasesForGradingPort loadTestCasesForGradingPort;
    private final LoadActiveDatasetFilePathPort loadActiveDatasetFilePathPort;
    private final GenerateDatasetAccessUrlPort generateDatasetAccessUrlPort;
    private final CodeGradingService codeGradingService;

    @Override
    public GradingResult grade(Long problemSetId, Long problemId, String code) {
        submissionCodePolicy.validate(code);

        String filePath = loadActiveDatasetFilePathPort.loadActiveDatasetFilePath(problemSetId);
        String datasetAccessUrl = filePath == null || filePath.isBlank()
                ? null
                : generateDatasetAccessUrlPort.generate(filePath);

        var result = codeGradingService.grade(
                code,
                datasetAccessUrl,
                loadTestCasesForGradingPort.loadActiveTestCases(problemId)
        );

        return new GradingResult(
                Boolean.TRUE.equals(result.correct()),
                result.passedTestCount(),
                result.totalTestCount(),
                result.executionStatus(),
                result.errorMessage()
        );
    }
}
