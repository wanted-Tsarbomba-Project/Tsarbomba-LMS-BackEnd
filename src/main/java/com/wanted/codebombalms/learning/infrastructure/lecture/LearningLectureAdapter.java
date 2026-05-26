package com.wanted.codebombalms.learning.infrastructure.lecture;

import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
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
    public List<Long> findLectureIdsByCourse(Long courseId) {
        return lectureRepository.findByCourse_CourseIdAndDeletedAtIsNull(courseId)
                .stream()
                .map(LectureJpaEntity::getLectureId)
                .toList();
    }
}
