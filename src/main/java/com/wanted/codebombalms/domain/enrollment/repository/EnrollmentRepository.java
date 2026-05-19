package com.wanted.codebombalms.domain.enrollment.repository;

import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.enrollment.entity.Enrollment;
import com.wanted.codebombalms.domain.enrollment.enums.EnrollmentStatus;
import com.wanted.codebombalms.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * 특정 학생이 특정 강좌를 현재 수강 중인지 확인
     */
    boolean existsByCourseAndStudentAndStatus(
            Course course,
            User student,
            EnrollmentStatus status
    );

    /**
     * 학생의 수강 중인 강좌 목록 조회
     */
    List<Enrollment> findByStudentAndStatus(
            User student,
            EnrollmentStatus status
    );

    /**
     * 수강 신청 ID와 상태로 조회
     */
    Optional<Enrollment> findByEnrollmentIdAndStatus(
            Long enrollmentId,
            EnrollmentStatus status
    );

    /**
     * 특정 학생의 특정 수강 신청 조회
     */
    Optional<Enrollment> findByEnrollmentIdAndStudentAndStatus(
            Long enrollmentId,
            User student,
            EnrollmentStatus status
    );
}