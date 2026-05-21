package com.wanted.codebombalms.domain.enrollment.service;

import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.course.exception.CourseErrorCode;
import com.wanted.codebombalms.domain.course.repository.CourseRepository;
import com.wanted.codebombalms.domain.enrollment.dto.request.EnrollmentCreateRequest;
import com.wanted.codebombalms.domain.enrollment.dto.response.EnrollmentResponse;
import com.wanted.codebombalms.domain.enrollment.dto.response.MyCourseResponse;
import com.wanted.codebombalms.domain.enrollment.entity.Enrollment;
import com.wanted.codebombalms.domain.enrollment.enums.EnrollmentStatus;
import com.wanted.codebombalms.domain.enrollment.exception.EnrollmentErrorCode;
import com.wanted.codebombalms.domain.enrollment.repository.EnrollmentRepository;
import com.wanted.codebombalms.domain.user.infrastructure.persistence.SpringDataUserRepository;
import com.wanted.codebombalms.domain.user.infrastructure.persistence.UserJpaEntity;
import com.wanted.codebombalms.domain.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
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
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final SpringDataUserRepository userRepository;

    /**
     * 수강 신청
     */
    @Transactional
    public EnrollmentResponse createEnrollment(Long courseId, EnrollmentCreateRequest request) {

        Long studentId = request.getStudentId();

        log.info("[EnrollmentService] 수강 신청 시작 - courseId: {}, studentId: {}", courseId, studentId);

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        UserJpaEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        boolean alreadyEnrolled = enrollmentRepository.existsByCourseAndStudentAndStatus(
                course,
                student,
                EnrollmentStatus.ACTIVE
        );

        if (alreadyEnrolled) {
            throw new ConflictException(EnrollmentErrorCode.DUPLICATE_ENROLLMENT);
        }

        Enrollment enrollment = Enrollment.create(course, student);

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        log.info("[EnrollmentService] 수강 신청 완료 - enrollmentId: {}", savedEnrollment.getEnrollmentId());

        return EnrollmentResponse.from(savedEnrollment);
    }

    /**
     * 내 수강 강좌 목록 조회
     */
    public List<MyCourseResponse> findMyCourses(Long studentId) {

        log.info("[EnrollmentService] 내 수강 강좌 목록 조회 시작 - studentId: {}", studentId);

        UserJpaEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        List<MyCourseResponse> myCourses = enrollmentRepository
                .findByStudentAndStatus(student, EnrollmentStatus.ACTIVE)
                .stream()
                .map(MyCourseResponse::from)
                .toList();

        log.info("[EnrollmentService] 내 수강 강좌 목록 조회 완료 - studentId: {}, count: {}",
                studentId,
                myCourses.size()
        );

        return myCourses;
    }

    /**
     * 수강 신청 취소
     */
    @Transactional
    public void cancelEnrollment(Long studentId, Long enrollmentId) {

        log.info("[EnrollmentService] 수강 신청 취소 시작 - studentId: {}, enrollmentId: {}",
                studentId,
                enrollmentId
        );

        UserJpaEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        Enrollment enrollment = enrollmentRepository
                .findByEnrollmentIdAndStudentAndStatus(
                        enrollmentId,
                        student,
                        EnrollmentStatus.ACTIVE
                )
                .orElseThrow(() -> new NotFoundException(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND));

        enrollment.cancel();

        log.info("[EnrollmentService] 수강 신청 취소 완료 - enrollmentId: {}", enrollmentId);
    }

}