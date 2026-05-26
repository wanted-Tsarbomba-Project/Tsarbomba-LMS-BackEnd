package com.wanted.codebombalms.course.infrastructure.lecture;

import com.wanted.codebombalms.course.application.port.LectureManagementPort;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureManagementAdapter implements LectureManagementPort {

    private final LectureRepository lectureRepository;

    @Override
    public void deleteLecturesByCourseId(Long courseId) {
        lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId)
                .forEach(lecture -> {
                    lecture.delete();
                    lectureRepository.save(lecture);
                });
    }
}
