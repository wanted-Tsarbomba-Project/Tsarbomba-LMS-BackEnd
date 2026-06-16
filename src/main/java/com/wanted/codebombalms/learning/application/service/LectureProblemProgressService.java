package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.learning.application.command.RecordLectureProblemProgressCommand;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSetPort;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemProgressCommandUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressCommandUseCase;
import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.domain.repository.LectureProblemProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureProblemProgressService implements LectureProblemProgressCommandUseCase {

    private final LectureProblemProgressRepository lectureProblemProgressRepository;
    private final LearningLectureProblemSetPort learningLectureProblemSetPort;
    private final LectureProgressCommandUseCase lectureProgressCommandUseCase;

    @Override
    @Transactional
    public LectureProblemProgress recordProgress(RecordLectureProblemProgressCommand command) {
        LectureProblemProgress progress = lectureProblemProgressRepository
                .findByUserIdAndLectureProblemSetId(command.userId(), command.lectureProblemSetId())
                .orElseGet(() -> LectureProblemProgress.create(command.userId(), command.lectureProblemSetId()));

        progress.updateProgress(command.currentProblemNumber(), command.completed());
        LectureProblemProgress savedProgress = lectureProblemProgressRepository.save(progress);

        if (command.completed()) {
            var lectureProblemSet = learningLectureProblemSetPort.findLectureProblemSet(command.lectureProblemSetId());
            lectureProgressCommandUseCase.completeProgress(
                    command.userId(),
                    lectureProblemSet.lectureId()
            );
        }

        return savedProgress;
    }
}
