package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LectureProblemSubmissionRepositoryAdapter implements LectureProblemSubmissionRepository {

    private final SpringDataLectureProblemSubmissionRepository springDataLectureProblemSubmissionRepository;

    @Override
    public LectureProblemSubmission save(LectureProblemSubmission lectureProblemSubmission) {
        return springDataLectureProblemSubmissionRepository
                .save(LectureProblemSubmissionJpaEntity.from(lectureProblemSubmission))
                .toDomain();
    }

    @Override
    public int countAttempts(Long userId, Long courseProblemStepId) {
        return springDataLectureProblemSubmissionRepository.countByUserIdAndCourseProblemStepId(
                userId,
                courseProblemStepId
        );
    }
}
