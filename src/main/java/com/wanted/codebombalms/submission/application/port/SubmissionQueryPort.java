package com.wanted.codebombalms.submission.application.port;

import com.wanted.codebombalms.submission.domain.model.LatestSubmission;
import java.util.Optional;

public interface SubmissionQueryPort {

    Optional<LatestSubmission> findLatestResult(Long userId, Long problemId);
}
