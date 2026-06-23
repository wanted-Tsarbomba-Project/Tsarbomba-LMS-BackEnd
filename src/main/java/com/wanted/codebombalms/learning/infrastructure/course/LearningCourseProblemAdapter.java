package com.wanted.codebombalms.learning.infrastructure.course;

import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetQueryUseCase;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningCourseProblemAdapter implements LearningCourseProblemPort {

    private final LectureProblemSetQueryUseCase lectureProblemSetQueryUseCase;

    @Override
    public List<Long> findMainLectureProblemSetIdsByCourse(Long courseId) {
        return lectureProblemSetQueryUseCase.findProblemSetsByCourseAndRole(
                        courseId,
                        LectureProblemSetRole.MAIN
                )
                .stream()
                .map(LectureProblemSet::getLectureProblemSetId)
                .toList();
    }

    @Override
    public List<Long> findLectureProblemSetIdsByLecture(Long lectureId) {
        return lectureProblemSetQueryUseCase.findProblemSetsByLecture(lectureId)
                .stream()
                .map(LectureProblemSet::getLectureProblemSetId)
                .toList();
    }
}
