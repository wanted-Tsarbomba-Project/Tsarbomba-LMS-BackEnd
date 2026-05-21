package com.wanted.codebombalms.domain.lecture.application.service;

import com.wanted.codebombalms.domain.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.domain.lecture.domain.model.Lecture;
import com.wanted.codebombalms.domain.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.domain.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureUpdateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.response.LectureDetailResponse;
import com.wanted.codebombalms.domain.lecture.presentation.api.response.LectureResponse;
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
public class LectureService {

    private static final Logger log = LoggerFactory.getLogger(LectureService.class);

    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public LectureDetailResponse createLecture(Long courseId, LectureCreateRequest request) {
        log.info("[LectureService] create lecture - courseId: {}, title: {}", courseId, request.getTitle());

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        Lecture lecture = Lecture.create(
                course,
                request.getTitle(),
                request.getDescription(),
                request.getVideoUrl(),
                request.getThumbnailUrl(),
                request.getLectureOrder(),
                request.getStatus()
        );

        return LectureDetailResponse.from(lectureRepository.save(lecture));
    }

    public List<LectureResponse> findLecturesByCourseId(Long courseId) {
        log.info("[LectureService] find lectures - courseId: {}", courseId);

        courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        return lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId)
                .stream()
                .map(LectureResponse::from)
                .toList();
    }

    public LectureDetailResponse findLectureById(Long lectureId) {
        log.info("[LectureService] find lecture - lectureId: {}", lectureId);

        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));

        return LectureDetailResponse.from(lecture);
    }

    @Transactional
    public LectureDetailResponse updateLecture(Long lectureId, LectureUpdateRequest request) {
        log.info("[LectureService] update lecture - lectureId: {}", lectureId);

        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));

        lecture.update(
                request.getTitle(),
                request.getDescription(),
                request.getVideoUrl(),
                request.getThumbnailUrl(),
                request.getLectureOrder(),
                request.getStatus()
        );

        return LectureDetailResponse.from(lectureRepository.save(lecture));
    }

    @Transactional
    public void deleteLecture(Long lectureId) {
        log.info("[LectureService] delete lecture - lectureId: {}", lectureId);

        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));

        lecture.delete();
        lectureRepository.save(lecture);
    }
}
