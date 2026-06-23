package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.lecture.application.port.CourseCatalogPort;
import com.wanted.codebombalms.lecture.application.policy.LectureAccessPolicy;
import com.wanted.codebombalms.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureQueryService implements LectureQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(LectureQueryService.class);

    private final LectureRepository lectureRepository;
    private final CourseCatalogPort courseCatalogPort;
    private final LectureAccessPolicy lectureAccessPolicy;

    @Override
    public List<Lecture> findLecturesByCourseId(Long courseId) {
        log.info("[LectureQueryService] find lectures - courseId: {}", courseId);

        courseCatalogPort.findCourse(courseId);

        return lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId)
                .stream()
                .toList();
    }

    @Override
    public Lecture findLectureById(Long lectureId) {
        log.info("[LectureQueryService] find lecture - lectureId: {}", lectureId);

        return findExistingLecture(lectureId);
    }

    @Override
    public Lecture findLectureByIdForLearning(Long lectureId, Long userId, boolean operator) {
        log.info("[LectureQueryService] find lecture for learning - lectureId: {}, userId: {}", lectureId, userId);

        Lecture lecture = findExistingLecture(lectureId);
        lectureAccessPolicy.validateLearningContentAccess(lecture, userId, operator);
        if (!operator) {
            lectureAccessPolicy.validatePreviousLecturesCompleted(
                    userId,
                    lectureRepository.findPreviousLectureIds(
                            lecture.getCourse().getCourseId(),
                            lecture.getLectureOrder()
                    )
            );
        }

        return lecture;
    }

    private Lecture findExistingLecture(Long lectureId) {
        return lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));
    }
}
