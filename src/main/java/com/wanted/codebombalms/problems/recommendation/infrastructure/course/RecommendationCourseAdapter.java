package com.wanted.codebombalms.problems.recommendation.infrastructure.course;

import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.infrastructure.persistence.SpringDataCourseRepository;
import com.wanted.codebombalms.problems.recommendation.application.port.LoadRecommendationCoursePort;
import com.wanted.codebombalms.problems.recommendation.application.port.LoadRecommendationCoursePort.SelectableCourseData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecommendationCourseAdapter implements LoadRecommendationCoursePort {

    private final SpringDataCourseRepository courseRepository;

    @Override
    public Set<Long> loadActiveCourseIds(Set<Long> courseIds) {
        return courseRepository.findByCourseIdInAndStatusAndDeletedAtIsNull(
                        courseIds,
                        CourseStatus.ACTIVE
                )
                .stream()
                .map(course -> course.getCourseId())
                .collect(Collectors.toSet());
    }

    @Override
    public List<SelectableCourseData> loadSelectableCourses() {
        return courseRepository.findByStatusAndDeletedAtIsNull(CourseStatus.ACTIVE)
                .stream()
                .map(course -> new SelectableCourseData(
                        course.getCourseId(),
                        course.getTitle(),
                        course.getDescription(),
                        course.getThumbnailUrl()
                ))
                .toList();
    }
}
