package com.wanted.codebombalms.learning.infrastructure.lecture;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.port.LearningLecturePort;
import com.wanted.codebombalms.learning.application.port.LearningLecture;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningLectureAdapter implements LearningLecturePort {

    private final LectureQueryUseCase lectureQueryUseCase;

    @Override
    public boolean existsLecture(Long lectureId) {
        try {
            lectureQueryUseCase.findLectureById(lectureId);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public Long findCourseIdByLecture(Long lectureId) {
        try {
            return lectureQueryUseCase.findLectureById(lectureId).getCourse().getCourseId();
        } catch (NotFoundException e) {
            throw new NotFoundException(LearningErrorCode.LECTURE_NOT_FOUND);
        }
    }

    @Override
    public List<Long> findLectureIdsByCourse(Long courseId) {
        return lectureQueryUseCase.findLecturesByCourseId(courseId)
                .stream()
                .map(Lecture::getLectureId)
                .toList();
    }

    @Override
    public List<LearningLecture> findLecturesByCourse(Long courseId) {
        return lectureQueryUseCase.findLecturesByCourseId(courseId)
                .stream()
                .map(lecture -> new LearningLecture(lecture.getLectureId(), lecture.getTitle()))
                .toList();
    }
}
