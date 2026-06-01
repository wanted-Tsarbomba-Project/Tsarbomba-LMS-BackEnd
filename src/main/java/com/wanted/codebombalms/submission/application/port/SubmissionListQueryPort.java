package com.wanted.codebombalms.submission.application.port;

import com.wanted.codebombalms.submission.domain.model.CodeSubmissionPage;

public interface SubmissionListQueryPort {

    CodeSubmissionPage findCodeSubmissions(Long userId, Long problemId, int page, int size);
}
