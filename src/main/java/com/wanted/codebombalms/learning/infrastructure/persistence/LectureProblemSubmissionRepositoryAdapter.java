package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemSubmissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LectureProblemSubmissionRepositoryAdapter implements LectureProblemSubmissionRepository {

    private final SpringDataLectureProblemSubmissionRepository repository;

    @Override
    public LectureProblemSubmission save(LectureProblemSubmission submission) {
        return repository.save(LectureProblemSubmissionJpaEntity.from(submission)).toDomain();
    }

    @Override
    public int countAttempts(Long userId, Long lectureProblemSetId, Long problemId) {
        return repository.countByUserIdAndLectureProblemSetIdAndProblemId(
                userId,
                lectureProblemSetId,
                problemId
        );
    }

    @Override
    public List<LectureProblemSubmission> findByUserIdAndLectureProblemSetId(
            Long userId,
            Long lectureProblemSetId
    ) {
        return repository.findByUserIdAndLectureProblemSetIdOrderBySubmittedAtDescLectureProblemSubmissionIdDesc(
                        userId,
                        lectureProblemSetId
                )
                .stream()
                .map(LectureProblemSubmissionJpaEntity::toDomain)
                .toList();
    }
}
