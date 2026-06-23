package com.wanted.codebombalms.learning.domain.repository;

import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import java.util.List;

public interface LectureProblemSubmissionRepository {

    LectureProblemSubmission save(LectureProblemSubmission submission);

    int countAttempts(Long userId, Long lectureProblemSetId, Long problemId);

    List<LectureProblemSubmission> findByUserIdAndLectureProblemSetId(
            Long userId,
            Long lectureProblemSetId
    );
}
