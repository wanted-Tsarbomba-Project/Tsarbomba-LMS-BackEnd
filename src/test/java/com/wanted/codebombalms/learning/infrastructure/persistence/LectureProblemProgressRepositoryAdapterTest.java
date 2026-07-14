package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class LectureProblemProgressRepositoryAdapterTest {

    @Mock
    private SpringDataLectureProblemProgressRepository springDataLectureProblemProgressRepository;

    @InjectMocks
    private LectureProblemProgressRepositoryAdapter lectureProblemProgressRepositoryAdapter;

    @Test
    void save_returnsWinnerRow_whenConcurrentFirstEntryInsertConflicts() {
        Long userId = 10L;
        Long lectureProblemSetId = 6001L;
        LectureProblemProgress newProgress = LectureProblemProgress.create(userId, lectureProblemSetId);
        LectureProblemProgress existingProgress = LectureProblemProgress.restore(
                999L,
                userId,
                lectureProblemSetId,
                1,
                false,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        LectureProblemProgressJpaEntity existingEntity = LectureProblemProgressJpaEntity.from(existingProgress);

        willThrow(new DataIntegrityViolationException("duplicate key"))
                .given(springDataLectureProblemProgressRepository)
                .save(any(LectureProblemProgressJpaEntity.class));
        given(springDataLectureProblemProgressRepository
                .findByUserIdAndLectureProblemSetId(userId, lectureProblemSetId))
                .willReturn(Optional.of(existingEntity));

        LectureProblemProgress result = lectureProblemProgressRepositoryAdapter.save(newProgress);

        assertEquals(999L, result.getLectureProblemProgressId());
        assertEquals(userId, result.getUserId());
        assertEquals(lectureProblemSetId, result.getLectureProblemSetId());
    }
}
