package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.lecture.application.port.CourseCatalogPort;
import com.wanted.codebombalms.lecture.application.policy.LectureAccessPolicy;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetQueryUseCase;
import com.wanted.codebombalms.lecture.domain.exception.LectureProblemSetErrorCode;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureProblemSetRepository;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureProblemSetQueryService implements LectureProblemSetQueryUseCase {

    private final LectureProblemSetRepository lectureProblemSetRepository;
    private final CourseCatalogPort courseCatalogPort;
    private final LectureRepository lectureRepository;
    private final LectureAccessPolicy lectureAccessPolicy;

    @Override
    @Transactional(readOnly = true)
    public List<LectureProblemSet> findProblemSetsByCourse(Long courseId) {
        return lectureProblemSetRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureProblemSet> findProblemSetsByCourseForAccess(Long courseId, Long userId, boolean operator) {
        var course = courseCatalogPort.findCourse(courseId);
        lectureAccessPolicy.validateCourseContentAccess(course, userId, operator);
        return lectureProblemSetRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureProblemSet> findProblemSetsByCourseAndRole(Long courseId, LectureProblemSetRole role) {
        return lectureProblemSetRepository.findByCourseIdAndRole(courseId, role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureProblemSet> findProblemSetsByLecture(Long lectureId) {
        return lectureProblemSetRepository.findByLectureId(lectureId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LectureProblemSet> findProblemSetsByLectureForAccess(Long lectureId, Long userId, boolean operator) {
        var lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));
        lectureAccessPolicy.validateCourseContentAccess(lecture.getCourse(), userId, operator);
        return lectureProblemSetRepository.findByLectureId(lectureId);
    }

    @Override
    @Transactional(readOnly = true)
    public LectureProblemSet findProblemSetById(Long lectureProblemSetId) {
        return lectureProblemSetRepository.findById(lectureProblemSetId)
                .orElseThrow(() -> new NotFoundException(LectureProblemSetErrorCode.PROBLEM_SET_NOT_FOUND));
    }
}
