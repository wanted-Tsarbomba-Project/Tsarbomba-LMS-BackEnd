package com.wanted.codebombalms.submission.application.port;

import com.wanted.codebombalms.submission.domain.model.CodeSubmissionPage;

public interface SubmissionListQueryPort {

    CodeSubmissionPage findCodeSubmissions(Long problemId, int page, int size);
}
