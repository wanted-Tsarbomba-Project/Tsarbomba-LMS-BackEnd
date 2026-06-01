package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CourseRepositoryAdapter implements CourseRepository {

    private final SpringDataCourseRepository springDataCourseRepository;
    private final SpringDataCourseCategoryRepository springDataCourseCategoryRepository;

    @Override
    public Course save(Course course) {
        CourseCategoryJpaEntity courseCategory = course.getCourseCategoryId() == null
                ? null
                : springDataCourseCategoryRepository.findById(course.getCourseCategoryId())
                .orElseThrow();

        CourseJpaEntity entity = course.getCourseId() == null
                ? CourseJpaEntity.from(course, courseCategory)
                : springDataCourseRepository.findById(course.getCourseId())
                .map(found -> {
                    found.apply(course, courseCategory);
                    return found;
                })
                .orElseGet(() -> CourseJpaEntity.from(course, courseCategory));

        return springDataCourseRepository.save(entity).toDomain();
    }

    @Override
    public List<Course> findByDeletedAtIsNull() {
        return springDataCourseRepository.findByDeletedAtIsNull()
                .stream()
                .map(CourseJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Course> findByStatusAndDeletedAtIsNull(CourseStatus status) {
        return springDataCourseRepository.findByStatusAndDeletedAtIsNull(status)
                .stream()
                .map(CourseJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Course> findByCourseCategoryIdAndStatusAndDeletedAtIsNull(Long courseCategoryId, CourseStatus status) {
        return springDataCourseRepository
                .findByCourseCategory_CourseCategoryIdAndStatusAndDeletedAtIsNull(courseCategoryId, status)
                .stream()
                .map(CourseJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Course> findByCourseCategoryIdAndDeletedAtIsNull(Long courseCategoryId) {
        return springDataCourseRepository
                .findByCourseCategory_CourseCategoryIdAndDeletedAtIsNull(courseCategoryId)
                .stream()
                .map(CourseJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Course> findByCourseIdAndDeletedAtIsNull(Long courseId) {
        return springDataCourseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .map(CourseJpaEntity::toDomain);
    }

    @Override
    public Optional<Course> findByCourseIdAndStatusAndDeletedAtIsNull(Long courseId, CourseStatus status) {
        return springDataCourseRepository.findByCourseIdAndStatusAndDeletedAtIsNull(courseId, status)
                .map(CourseJpaEntity::toDomain);
    }

    @Override
    public List<Course> findByInstructorIdAndDeletedAtIsNull(Long instructorId) {
        return springDataCourseRepository.findByInstructorIdAndDeletedAtIsNull(instructorId)
                .stream()
                .map(CourseJpaEntity::toDomain)
                .toList();
    }
}
