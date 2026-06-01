package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.command.RecordLectureProgressCommand;
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

    @Override
    @Transactional
    public LectureProgress recordProgress(RecordLectureProgressCommand command) {
        if (!learningLecturePort.existsLecture(command.lectureId())) {
            throw new NotFoundException(LearningErrorCode.LECTURE_NOT_FOUND);
        }

        LectureProgress progress = lectureProgressRepository
                .findByUserIdAndLectureId(command.userId(), command.lectureId())
                .orElseGet(() -> LectureProgress.create(command.userId(), command.lectureId()));

        progress.record(command.completed());
        return lectureProgressRepository.save(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public LectureProgress findProgress(Long userId, Long lectureId) {
        if (!learningLecturePort.existsLecture(lectureId)) {
            throw new NotFoundException(LearningErrorCode.LECTURE_NOT_FOUND);
        }

        return lectureProgressRepository.findByUserIdAndLectureId(userId, lectureId)
                .orElseGet(() -> LectureProgress.create(userId, lectureId));
    }
}
