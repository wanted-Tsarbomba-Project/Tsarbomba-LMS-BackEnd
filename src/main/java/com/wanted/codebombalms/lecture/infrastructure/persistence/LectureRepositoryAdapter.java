package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.course.infrastructure.persistence.CourseJpaEntity;
import com.wanted.codebombalms.course.infrastructure.persistence.SpringDataCourseRepository;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LectureRepositoryAdapter implements LectureRepository {

    private final SpringDataLectureRepository springDataLectureRepository;
    private final SpringDataCourseRepository springDataCourseRepository;

    @Override
    public Lecture save(Lecture lecture) {
        CourseJpaEntity courseEntity = springDataCourseRepository.findById(lecture.getCourse().getCourseId())
                .orElseThrow();
        LectureJpaEntity entity = lecture.getLectureId() == null
                ? LectureJpaEntity.from(lecture, courseEntity)
                : springDataLectureRepository.findById(lecture.getLectureId())
                .map(found -> {
                    found.apply(lecture, courseEntity);
                    return found;
                })
                .orElseGet(() -> LectureJpaEntity.from(lecture, courseEntity));

        return springDataLectureRepository.save(entity).toDomain();
    }

    @Override
    public List<Lecture> findByDeletedAtIsNull() {
        return springDataLectureRepository.findByDeletedAtIsNull()
                .stream()
                .map(LectureJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Lecture> findByLectureIdAndDeletedAtIsNull(Long lectureId) {
        return springDataLectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .map(LectureJpaEntity::toDomain);
    }

    @Override
    public List<Lecture> findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(Long courseId) {
        return springDataLectureRepository.findByCourse_CourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId)
                .stream()
                .map(LectureJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCourseIdAndDeletedAtIsNull(Long courseId) {
        return springDataLectureRepository.existsByCourse_CourseIdAndDeletedAtIsNull(courseId);
    }

    @Override
    public boolean existsByCourseIdAndLectureIdAndDeletedAtIsNull(Long courseId, Long lectureId) {
        return springDataLectureRepository.existsByCourse_CourseIdAndLectureIdAndDeletedAtIsNull(courseId, lectureId);
    }
}
