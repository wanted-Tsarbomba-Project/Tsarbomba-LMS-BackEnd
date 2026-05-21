package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.submission.domain.model.LatestSubmission;
import com.wanted.codebombalms.submission.application.port.SubmissionQueryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SubmissionQueryService {

    private final SubmissionQueryPort submissionQueryPort;

    public SubmissionQueryService(SubmissionQueryPort submissionQueryPort) {
        this.submissionQueryPort = submissionQueryPort;
    }

    public Optional<LatestSubmission> findLatestResult(Long userId, Long problemId) {
        return submissionQueryPort.findLatestResult(userId, problemId);
    }
}
