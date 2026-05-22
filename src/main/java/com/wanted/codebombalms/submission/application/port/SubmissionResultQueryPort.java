package com.wanted.codebombalms.submission.application.port;

import com.wanted.codebombalms.submission.domain.model.CodeSubmissionResult;

public interface SubmissionResultQueryPort {

    CodeSubmissionResult getCodeSubmissionResult(Long submissionId);
}
