package com.wanted.codebombalms.submission.application.port;

import com.wanted.codebombalms.submission.domain.model.CodeSubmission;
import com.wanted.codebombalms.submission.domain.model.SubmissionTestResult;

import java.util.List;

public interface SubmissionCommandPort {

    int countAttempts(Long userId, Long problemId);

    Long saveCodeSubmission(CodeSubmission submission);

    void saveTestResults(Long submissionId, List<SubmissionTestResult> testResults);

    boolean existsCorrectSubmission(Long userId, Long problemId);
}
