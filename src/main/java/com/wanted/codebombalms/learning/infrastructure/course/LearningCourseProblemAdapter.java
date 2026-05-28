package com.wanted.codebombalms.learning.infrastructure.course;

import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseProblemSetJpaEntity;
import com.wanted.codebombalms.course.infrastructure.persistence.SpringDataCourseProblemSetRepository;
import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningCourseProblemAdapter implements LearningCourseProblemPort {

    private final SpringDataCourseProblemSetRepository courseProblemSetRepository;

    @Override
    public List<Long> findMainLectureProblemSetIdsByCourse(Long courseId) {
        return courseProblemSetRepository.findActiveByCourseIdAndRole(
                        courseId,
                        CourseProblemSetRole.MAIN
                )
                .stream()
                .map(CourseProblemSetJpaEntity::getCourseProblemSetId)
                .toList();
    }

    @Override
    public List<Long> findLectureProblemSetIdsByLecture(Long lectureId) {
        return courseProblemSetRepository.findActiveByLectureIdOrderByDisplayOrderAsc(lectureId)
                .stream()
                .map(CourseProblemSetJpaEntity::getCourseProblemSetId)
                .toList();
    }
}
