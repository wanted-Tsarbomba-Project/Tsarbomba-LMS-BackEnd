package com.wanted.codebombalms.learning.domain.repository;

import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;

public interface LectureProblemSubmissionRepository {

    LectureProblemSubmission save(LectureProblemSubmission lectureProblemSubmission);

    int countAttempts(Long userId, Long courseProblemStepId);
}
