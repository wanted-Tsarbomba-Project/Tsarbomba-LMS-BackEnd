package com.wanted.codebombalms.learning.infrastructure.course;

import com.wanted.codebombalms.course.application.usecase.CourseProblemQueryUseCase;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSet;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSetPort;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningLectureProblemSetAdapter implements LearningLectureProblemSetPort {

    private final CourseProblemQueryUseCase courseProblemQueryUseCase;

    @Override
    public LearningLectureProblemSet findLectureProblemSet(Long lectureProblemSetId) {
        CourseProblemSet lectureProblemSet;
        try {
            lectureProblemSet = courseProblemQueryUseCase.findProblemSetById(lectureProblemSetId);
        } catch (NotFoundException e) {
            throw new NotFoundException(LearningErrorCode.LECTURE_PROBLEM_SET_NOT_FOUND, e);
        }

        return new LearningLectureProblemSet(
                lectureProblemSet.getCourseProblemSetId(),
                lectureProblemSet.getLectureId(),
                lectureProblemSet.getProblemSetId()
        );
    }
}
