package com.wanted.codebombalms.domain.enrollment.service;

import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.course.exception.CourseNotFoundException;
import com.wanted.codebombalms.domain.course.repository.CourseRepository;
import com.wanted.codebombalms.domain.enrollment.dto.request.EnrollmentCreateRequest;
import com.wanted.codebombalms.domain.enrollment.dto.response.EnrollmentResponse;
import com.wanted.codebombalms.domain.enrollment.entity.Enrollment;
import com.wanted.codebombalms.domain.enrollment.enums.EnrollmentStatus;
import com.wanted.codebombalms.domain.enrollment.exception.EnrollmentNotFoundException;
import com.wanted.codebombalms.domain.enrollment.exception.DuplicateEnrollmentException;
import com.wanted.codebombalms.domain.enrollment.dto.response.MyCourseResponse;
import java.util.List;
import com.wanted.codebombalms.domain.enrollment.repository.EnrollmentRepository;
import com.wanted.codebombalms.user.infrastructure.persistence.UserJpaEntity;
import com.wanted.codebombalms.user.infrastructure.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        UserJpaEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId: " + studentId));

        boolean alreadyEnrolled = enrollmentRepository.existsByCourseAndStudentAndStatus(
                course,
                student,
                EnrollmentStatus.ACTIVE
        );

        if (alreadyEnrolled) {
            throw new DuplicateEnrollmentException(courseId, studentId);
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId: " + studentId));

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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId: " + studentId));

        Enrollment enrollment = enrollmentRepository
                .findByEnrollmentIdAndStudentAndStatus(
                        enrollmentId,
                        student,
                        EnrollmentStatus.ACTIVE
                )
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        enrollment.cancel();

        log.info("[EnrollmentService] 수강 신청 취소 완료 - enrollmentId: {}", enrollmentId);
    }

}