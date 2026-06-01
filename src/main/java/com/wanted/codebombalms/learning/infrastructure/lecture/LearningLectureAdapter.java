package com.wanted.codebombalms.learning.infrastructure.lecture;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import com.wanted.codebombalms.learning.application.port.LearningLecture;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.lecture.infrastructure.persistence.LectureJpaEntity;
import com.wanted.codebombalms.lecture.infrastructure.persistence.SpringDataLectureRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningLectureAdapter implements LearningLecturePort {

    private final SpringDataLectureRepository lectureRepository;

    @Override
    public boolean existsLecture(Long lectureId) {
        return lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId).isPresent();
    }

    @Override
    public Long findCourseIdByLecture(Long lectureId) {
        return lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .map(lecture -> lecture.getCourse().getCourseId())
                .orElseThrow(() -> new NotFoundException(LearningErrorCode.LECTURE_NOT_FOUND));
    }

    @Override
    public List<Long> findLectureIdsByCourse(Long courseId) {
        return lectureRepository.findByCourse_CourseIdAndDeletedAtIsNull(courseId)
                .stream()
                .map(LectureJpaEntity::getLectureId)
                .toList();
    }

    @Override
    public List<LearningLecture> findLecturesByCourse(Long courseId) {
        return lectureRepository.findByCourse_CourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId)
                .stream()
                .map(lecture -> new LearningLecture(lecture.getLectureId(), lecture.getTitle()))
                .toList();
    }
}
