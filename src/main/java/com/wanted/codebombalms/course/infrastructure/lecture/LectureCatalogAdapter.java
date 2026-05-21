package com.wanted.codebombalms.course.infrastructure.lecture;

import com.wanted.codebombalms.course.application.port.LectureCatalogPort;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureCatalogAdapter implements LectureCatalogPort {

    private final LectureRepository lectureRepository;

    @Override
    public boolean existsLectureInCourse(Long courseId) {
        return lectureRepository.existsByCourseIdAndDeletedAtIsNull(courseId);
    }
}
