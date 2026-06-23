package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.lecture.application.port.CourseCatalogPort;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LectureRepositoryAdapter implements LectureRepository {

    private final SpringDataLectureRepository springDataLectureRepository;
    private final CourseCatalogPort courseCatalogPort;

    @Override
    public Lecture save(Lecture lecture) {
        LectureJpaEntity entity = lecture.getLectureId() == null
                ? LectureJpaEntity.from(lecture)
                : springDataLectureRepository.findById(lecture.getLectureId())
                .map(found -> {
                    found.apply(lecture);
                    return found;
                })
                .orElseGet(() -> LectureJpaEntity.from(lecture));

        return springDataLectureRepository.save(entity).toDomain(lecture.getCourse());
    }

    @Override
    public List<Lecture> findByDeletedAtIsNull() {
        List<LectureJpaEntity> entities = springDataLectureRepository.findByDeletedAtIsNull();
        if (entities.isEmpty()) {
            return List.of();
        }

        Set<Long> courseIds = entities.stream()
                .map(LectureJpaEntity::getCourseId)
                .collect(Collectors.toSet());
        Map<Long, Course> courses = courseCatalogPort.findCourses(courseIds);

        return entities
                .stream()
                .map(entity -> entity.toDomain(courses.get(entity.getCourseId())))
                .toList();
    }

    @Override
    public Optional<Lecture> findByLectureIdAndDeletedAtIsNull(Long lectureId) {
        return springDataLectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .map(this::toDomain);
    }

    @Override
    public List<Lecture> findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(Long courseId) {
        Course course = courseCatalogPort.findCourse(courseId);
        return springDataLectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId)
                .stream()
                .map(entity -> entity.toDomain(course))
                .toList();
    }

    @Override
    public List<Long> findPreviousLectureIds(Long courseId, Integer lectureOrder) {
        if (lectureOrder == null) {
            throw new IllegalArgumentException("lectureOrder must not be null");
        }
        return springDataLectureRepository.findPreviousLectureIds(courseId, lectureOrder);
    }

    @Override
    public boolean existsNextLecture(Long courseId, Integer lectureOrder) {
        return lectureOrder != null
                && springDataLectureRepository.existsByCourseIdAndDeletedAtIsNullAndLectureOrderGreaterThan(
                courseId,
                lectureOrder
        );
    }

    @Override
    public boolean existsByCourseIdAndDeletedAtIsNull(Long courseId) {
        return springDataLectureRepository.existsByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Override
    public boolean existsByCourseIdAndLectureIdAndDeletedAtIsNull(Long courseId, Long lectureId) {
        return springDataLectureRepository.existsByCourseIdAndLectureIdAndDeletedAtIsNull(courseId, lectureId);
    }

    private Lecture toDomain(LectureJpaEntity entity) {
        return entity.toDomain(courseCatalogPort.findCourse(entity.getCourseId()));
    }
}
