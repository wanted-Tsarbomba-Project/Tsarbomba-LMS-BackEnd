package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.learning.application.command.RecordLectureProgressCommand;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressCommandUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressQueryUseCase;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureProgressService implements LectureProgressCommandUseCase, LectureProgressQueryUseCase {

    private final LectureProgressRepository lectureProgressRepository;
    private final LearningLecturePort learningLecturePort;
    private final LearningEnrollmentPort learningEnrollmentPort;

    @Override
    @Transactional
    public LectureProgress recordProgress(RecordLectureProgressCommand command) {
        validateEnrollment(command.userId(), command.lectureId());
        validateProgress(command);

        LectureProgress progress = lectureProgressRepository
                .findByUserIdAndLectureId(command.userId(), command.lectureId())
                .orElseGet(() -> LectureProgress.create(command.userId(), command.lectureId()));

        validateDuration(command.durationSec(), progress.getDurationSec());
        validateLastPosition(command.lastPositionSec(), command.durationSec(), progress.getDurationSec());
        progress.recordVideoProgress(command.lastPositionSec(), command.durationSec(), command.watchedDeltaSec());
        return lectureProgressRepository.save(progress);
    }

    @Override
    @Transactional
    public LectureProgress completeProgress(Long userId, Long lectureId) {
        validateEnrollment(userId, lectureId);

        LectureProgress progress = lectureProgressRepository
                .findByUserIdAndLectureId(userId, lectureId)
                .orElseGet(() -> LectureProgress.create(userId, lectureId));

        progress.complete();
        return lectureProgressRepository.save(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public LectureProgress findProgress(Long userId, Long lectureId) {
        validateEnrollment(userId, lectureId);

        return lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId)
                .orElseGet(() -> LectureProgress.create(userId, lectureId));
    }

    private void validateEnrollment(Long userId, Long lectureId) {
        if (!learningLecturePort.existsLecture(lectureId)) {
            throw new NotFoundException(LearningErrorCode.LECTURE_NOT_FOUND);
        }

        Long courseId = learningLecturePort.findCourseIdByLecture(lectureId);
        if (!learningEnrollmentPort.isActiveStudentOfCourse(courseId, userId)) {
            throw new ForbiddenException(LearningErrorCode.LECTURE_PROGRESS_ACCESS_DENIED);
        }
    }

    private void validateProgress(RecordLectureProgressCommand command) {
        if (command.lastPositionSec() < 0 || command.watchedDeltaSec() < 0) {
            throw new ValidationException(LearningErrorCode.INVALID_LECTURE_PROGRESS);
        }
        if (command.durationSec() != null && command.durationSec() <= 0) {
            throw new ValidationException(LearningErrorCode.INVALID_LECTURE_PROGRESS);
        }
    }

    private void validateDuration(Integer requestedDurationSec, Integer savedDurationSec) {
        if (requestedDurationSec != null && savedDurationSec != null && !requestedDurationSec.equals(savedDurationSec)) {
            throw new ValidationException(LearningErrorCode.INVALID_LECTURE_PROGRESS);
        }
    }

    private void validateLastPosition(int lastPositionSec, Integer requestedDurationSec, Integer savedDurationSec) {
        Integer durationSec = requestedDurationSec != null ? requestedDurationSec : savedDurationSec;
        if (durationSec != null && lastPositionSec > durationSec) {
            throw new ValidationException(LearningErrorCode.INVALID_LECTURE_PROGRESS);
        }
    }
}
