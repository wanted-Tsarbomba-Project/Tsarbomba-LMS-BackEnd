package com.wanted.codebombalms.problems.recommendation.infrastructure.course;

import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseJpaEntity;
import com.wanted.codebombalms.course.infrastructure.persistence.SpringDataCourseRepository;
import com.wanted.codebombalms.problems.recommendation.application.port.LoadRecommendationCoursePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    public List<SelectableCourseData> loadSelectableCourses(String keyword, int limit) {
        var pageable = PageRequest.of(0, limit);

        var courses = isBlank(keyword)
                ? courseRepository.findByStatusAndDeletedAtIsNullOrderByCourseIdDesc(
                CourseStatus.ACTIVE,
                pageable
        )
                : courseRepository.findByStatusAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByCourseIdDesc(
                CourseStatus.ACTIVE,
                keyword.trim(),
                pageable
        );

        return courses.stream()
                .map(this::toSelectableCourseData)
                .toList();
    }

    @Override
    public List<SelectableCourseData> loadActiveCoursesByIds(Set<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }

        return courseRepository.findByCourseIdInAndStatusAndDeletedAtIsNull(
                        courseIds,
                        CourseStatus.ACTIVE
                )
                .stream()
                .map(this::toSelectableCourseData)
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private SelectableCourseData toSelectableCourseData(CourseJpaEntity course) {
        var courseCategory = course.getCourseCategory();

        return new SelectableCourseData(
                course.getCourseId(),
                courseCategory != null ? courseCategory.getCourseCategoryId() : null,
                courseCategory != null ? courseCategory.getName() : null,
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl()
        );
    }
}
