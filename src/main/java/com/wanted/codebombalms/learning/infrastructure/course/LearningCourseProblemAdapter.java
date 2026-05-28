package com.wanted.codebombalms.learning.infrastructure.course;

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
    public List<Long> findLectureProblemSetIdsByCourse(Long courseId) {
        return courseProblemSetRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(CourseProblemSetJpaEntity::getCourseProblemSetId)
                .toList();
    }

    @Override
    public List<Long> findLectureProblemSetIdsByLecture(Long lectureId) {
        return courseProblemSetRepository.findByLectureIdOrderByDisplayOrderAsc(lectureId)
                .stream()
                .map(CourseProblemSetJpaEntity::getCourseProblemSetId)
                .toList();
    }
}
