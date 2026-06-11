package com.wanted.codebombalms.learning.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLectureProblemSubmissionRepository
        extends JpaRepository<LectureProblemSubmissionJpaEntity, Long> {

    int countByUserIdAndLectureProblemSetIdAndProblemId(
            Long userId,
            Long lectureProblemSetId,
            Long problemId
    );

    List<LectureProblemSubmissionJpaEntity>
    findByUserIdAndLectureProblemSetIdOrderBySubmittedAtDesc(
            Long userId,
            Long lectureProblemSetId
    );
}
