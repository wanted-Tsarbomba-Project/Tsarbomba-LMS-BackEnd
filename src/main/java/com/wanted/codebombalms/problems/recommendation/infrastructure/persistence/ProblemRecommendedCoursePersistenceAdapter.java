package com.wanted.codebombalms.problems.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.problems.recommendation.application.port.LoadSavedRecommendedCoursePort;
import com.wanted.codebombalms.problems.recommendation.application.port.LoadSavedRecommendedCoursePort.SavedRecommendedCourseData;
import com.wanted.codebombalms.problems.recommendation.application.port.SaveProblemRecommendedCoursePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProblemRecommendedCoursePersistenceAdapter implements
        SaveProblemRecommendedCoursePort,
        LoadSavedRecommendedCoursePort {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final SpringDataProblemRecommendedCourseRepository repository;

    @Override
    public void replaceRecommendedCourses(Long problemId, List<RecommendedCourseOrder> courses) {
        repository.deleteByProblemId(problemId);

        List<ProblemRecommendedCourseJpaEntity> entities = courses.stream()
                .map(course -> new ProblemRecommendedCourseJpaEntity(
                        problemId,
                        course.courseId(),
                        course.displayOrder()
                ))
                .toList();

        repository.saveAll(entities);
    }

    @Override
    public List<SavedRecommendedCourseData> loadSavedRecommendedCourses(Long problemId) {
        return repository.findByProblemIdAndStatusOrderByDisplayOrderAsc(
                        problemId,
                        ACTIVE_STATUS
                )
                .stream()
                .map(entity -> new SavedRecommendedCourseData(
                        entity.getCourseId(),
                        entity.getDisplayOrder()
                ))
                .toList();
    }
}
