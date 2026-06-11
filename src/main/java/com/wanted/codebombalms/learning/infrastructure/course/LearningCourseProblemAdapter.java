package com.wanted.codebombalms.learning.infrastructure.course;

import com.wanted.codebombalms.course.application.usecase.CourseProblemQueryUseCase;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningCourseProblemAdapter implements LearningCourseProblemPort {

    private final CourseProblemQueryUseCase courseProblemQueryUseCase;

    @Override
    public List<Long> findMainLectureProblemSetIdsByCourse(Long courseId) {
        return courseProblemQueryUseCase.findProblemSetsByCourseAndRole(
                        courseId,
                        CourseProblemSetRole.MAIN
                )
                .stream()
                .map(CourseProblemSet::getCourseProblemSetId)
                .toList();
    }

    @Override
    public List<Long> findLectureProblemSetIdsByLecture(Long lectureId) {
        return courseProblemQueryUseCase.findProblemSetsByLecture(lectureId)
                .stream()
                .map(CourseProblemSet::getCourseProblemSetId)
                .toList();
    }
}
