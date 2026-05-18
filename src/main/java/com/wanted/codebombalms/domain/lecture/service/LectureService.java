package com.wanted.codebombalms.domain.lecture.service;

import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.course.exception.CourseNotFoundException;
import com.wanted.codebombalms.domain.course.repository.CourseRepository;
import com.wanted.codebombalms.domain.lecture.dto.request.LectureCreateRequest;
import com.wanted.codebombalms.domain.lecture.dto.request.LectureUpdateRequest;
import com.wanted.codebombalms.domain.lecture.dto.response.LectureDetailResponse;
import com.wanted.codebombalms.domain.lecture.dto.response.LectureResponse;
import com.wanted.codebombalms.domain.lecture.entity.Lecture;
import com.wanted.codebombalms.domain.lecture.exception.LectureNotFoundException;
import com.wanted.codebombalms.domain.lecture.repository.LectureRepository;
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

    /**
     * 강의 등록
     */
    @Transactional
    public LectureDetailResponse createLecture(Long courseId, LectureCreateRequest request) {

        log.info("[LectureService] 강의 등록 시작 - courseId: {}, title: {}", courseId, request.getTitle());

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        Lecture lecture = Lecture.create(
                course,
                request.getTitle(),
                request.getDescription(),
                request.getVideoUrl(),
                request.getThumbnailUrl(),
                request.getLectureOrder(),
                request.getStatus()
        );

        Lecture savedLecture = lectureRepository.save(lecture);

        log.info("[LectureService] 강의 등록 완료 - lectureId: {}", savedLecture.getLectureId());

        return LectureDetailResponse.from(savedLecture);
    }

    /**
     * 특정 강좌의 강의 목록 조회
     */
    public List<LectureResponse> findLecturesByCourseId(Long courseId) {

        log.info("[LectureService] 강의 목록 조회 시작 - courseId: {}", courseId);

        courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        List<LectureResponse> lectures = lectureRepository
                .findByCourse_CourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId)
                .stream()
                .map(LectureResponse::from)
                .toList();

        log.info("[LectureService] 강의 목록 조회 완료 - courseId: {}, count: {}", courseId, lectures.size());

        return lectures;
    }

    /**
     * 강의 상세 조회
     */
    public LectureDetailResponse findLectureById(Long lectureId) {

        log.info("[LectureService] 강의 상세 조회 시작 - lectureId: {}", lectureId);

        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        log.info("[LectureService] 강의 상세 조회 완료 - lectureId: {}", lectureId);

        return LectureDetailResponse.from(lecture);
    }

    /**
     * 강의 수정
     */
    @Transactional
    public LectureDetailResponse updateLecture(Long lectureId, LectureUpdateRequest request) {

        log.info("[LectureService] 강의 수정 시작 - lectureId: {}", lectureId);

        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        lecture.update(
                request.getTitle(),
                request.getDescription(),
                request.getVideoUrl(),
                request.getThumbnailUrl(),
                request.getLectureOrder(),
                request.getStatus()
        );

        log.info("[LectureService] 강의 수정 완료 - lectureId: {}", lectureId);

        return LectureDetailResponse.from(lecture);
    }

    /**
     * 강의 삭제
     */
    @Transactional
    public void deleteLecture(Long lectureId) {

        log.info("[LectureService] 강의 삭제 시작 - lectureId: {}", lectureId);

        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new LectureNotFoundException(lectureId));

        lecture.delete();

        log.info("[LectureService] 강의 삭제 완료 - lectureId: {}", lectureId);
    }
}