package com.wanted.codebombalms.learning.infrastructure.course;

import com.wanted.codebombalms.course.infrastructure.persistence.CourseProblemStepJpaEntity;
import com.wanted.codebombalms.course.infrastructure.persistence.SpringDataCourseProblemStepRepository;
import com.wanted.codebombalms.learning.application.port.LearningCourseProblemPort;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningCourseProblemAdapter implements LearningCourseProblemPort {

    private final SpringDataCourseProblemStepRepository courseProblemStepRepository;

    @Override
    public Optional<CourseProblemStepInfo> findCourseProblemStep(Long courseProblemStepId) {
        return courseProblemStepRepository.findById(courseProblemStepId)
                .map(step -> new CourseProblemStepInfo(
                        step.getCourseProblemStepId(),
                        step.getProblemId(),
                        step.getLectureId()
                ));
    }

    @Override
    public List<Long> findCourseProblemStepIdsByCourse(Long courseId) {
        return courseProblemStepRepository.findByCourseProblemSet_Course_CourseIdOrderByStepOrderAsc(courseId)
                .stream()
                .map(CourseProblemStepJpaEntity::getCourseProblemStepId)
                .toList();
    }
}
