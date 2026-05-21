package com.wanted.codebombalms.submission.application.port;

import com.wanted.codebombalms.submission.domain.model.TextSubmission;

public interface SubmissionCommandPort {

    int countAttempts(Long userId, Long problemId);

    void saveTextSubmission(TextSubmission submission);
}
