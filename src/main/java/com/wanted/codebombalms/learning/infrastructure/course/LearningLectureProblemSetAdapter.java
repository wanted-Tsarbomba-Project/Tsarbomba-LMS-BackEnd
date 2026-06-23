package com.wanted.codebombalms.learning.infrastructure.course;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSet;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSetPort;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetQueryUseCase;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningLectureProblemSetAdapter implements LearningLectureProblemSetPort {

    private final LectureProblemSetQueryUseCase lectureProblemSetQueryUseCase;

    @Override
    public LearningLectureProblemSet findLectureProblemSet(Long lectureProblemSetId) {
        LectureProblemSet lectureProblemSet;
        try {
            lectureProblemSet = lectureProblemSetQueryUseCase.findProblemSetById(lectureProblemSetId);
        } catch (NotFoundException e) {
            throw new NotFoundException(LearningErrorCode.LECTURE_PROBLEM_SET_NOT_FOUND, e);
        }

        return new LearningLectureProblemSet(
                lectureProblemSet.getLectureProblemSetId(),
                lectureProblemSet.getCourseId(),
                lectureProblemSet.getLectureId(),
                lectureProblemSet.getProblemSetId()
        );
    }
}
