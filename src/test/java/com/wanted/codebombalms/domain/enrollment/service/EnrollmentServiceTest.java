package com.wanted.codebombalms.domain.enrollment.service;

import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.course.exception.CourseNotFoundException;
import com.wanted.codebombalms.domain.course.repository.CourseRepository;
import com.wanted.codebombalms.domain.enrollment.dto.request.EnrollmentCreateRequest;
import com.wanted.codebombalms.domain.enrollment.dto.response.EnrollmentResponse;
import com.wanted.codebombalms.domain.enrollment.dto.response.MyCourseResponse;
import com.wanted.codebombalms.domain.enrollment.entity.Enrollment;
import com.wanted.codebombalms.domain.enrollment.enums.EnrollmentStatus;
import com.wanted.codebombalms.domain.enrollment.exception.DuplicateEnrollmentException;
import com.wanted.codebombalms.domain.enrollment.exception.EnrollmentNotFoundException;
import com.wanted.codebombalms.domain.enrollment.repository.EnrollmentRepository;
import com.wanted.codebombalms.domain.user.entity.User;
import com.wanted.codebombalms.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.lenient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService 단위 테스트")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    @DisplayName("수강 신청 시 EnrollmentResponse를 반환한다.")
    void 수강_신청_테스트() {

        // given
        Long courseId = 1L;
        Long studentId = 10L;

        Course course = createCourse(courseId, "Java 기초 강좌");
        User student = createStudent(studentId);

        EnrollmentCreateRequest request = new EnrollmentCreateRequest(studentId);

        Enrollment savedEnrollment = createEnrollment(
                1L,
                course,
                student,
                EnrollmentStatus.ACTIVE
        );

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId))
                .willReturn(Optional.of(course));
        given(userRepository.findById(studentId))
                .willReturn(Optional.of(student));
        given(enrollmentRepository.existsByCourseAndStudentAndStatus(
                course,
                student,
                EnrollmentStatus.ACTIVE
        )).willReturn(false);
        given(enrollmentRepository.save(any(Enrollment.class)))
                .willReturn(savedEnrollment);

        // when
        EnrollmentResponse response = enrollmentService.createEnrollment(courseId, request);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getEnrollmentId());
        assertEquals(courseId, response.getCourseId());
        assertEquals(studentId, response.getStudentId());
        assertEquals(EnrollmentStatus.ACTIVE, response.getStatus());
        assertNull(response.getCanceledAt());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
        verify(userRepository).findById(studentId);
        verify(enrollmentRepository).existsByCourseAndStudentAndStatus(
                course,
                student,
                EnrollmentStatus.ACTIVE
        );
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("존재하지 않는 강좌에 수강 신청 시 CourseNotFoundException이 발생한다.")
    void 존재하지_않는_강좌_수강_신청_예외_테스트() {

        // given
        Long courseId = 999L;
        Long studentId = 10L;

        EnrollmentCreateRequest request = new EnrollmentCreateRequest(studentId);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId))
                .willReturn(Optional.empty());

        // when
        CourseNotFoundException exception = assertThrows(
                CourseNotFoundException.class,
                () -> enrollmentService.createEnrollment(courseId, request)
        );

        // then
        assertEquals(courseId, exception.getCourseId());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
    }

    @Test
    @DisplayName("이미 수강 신청한 강좌를 다시 신청하면 DuplicateEnrollmentException이 발생한다.")
    void 중복_수강_신청_예외_테스트() {

        // given
        Long courseId = 1L;
        Long studentId = 10L;

        Course course = createCourse(courseId, "Java 기초 강좌");
        User student = createStudent(studentId);

        EnrollmentCreateRequest request = new EnrollmentCreateRequest(studentId);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId))
                .willReturn(Optional.of(course));
        given(userRepository.findById(studentId))
                .willReturn(Optional.of(student));
        given(enrollmentRepository.existsByCourseAndStudentAndStatus(
                course,
                student,
                EnrollmentStatus.ACTIVE
        )).willReturn(true);

        // when
        DuplicateEnrollmentException exception = assertThrows(
                DuplicateEnrollmentException.class,
                () -> enrollmentService.createEnrollment(courseId, request)
        );

        // then
        assertEquals(courseId, exception.getCourseId());
        assertEquals(studentId, exception.getStudentId());

        verify(courseRepository).findByCourseIdAndDeletedAtIsNull(courseId);
        verify(userRepository).findById(studentId);
        verify(enrollmentRepository).existsByCourseAndStudentAndStatus(
                course,
                student,
                EnrollmentStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("학생의 수강 강좌 목록을 조회한다.")
    void 내_수강_강좌_목록_조회_테스트() {

        // given
        Long studentId = 10L;

        User student = createStudent(studentId);

        Course course1 = createCourse(1L, "Java 기초 강좌");
        Course course2 = createCourse(2L, "Spring 기초 강좌");

        Enrollment enrollment1 = createEnrollment(
                1L,
                course1,
                student,
                EnrollmentStatus.ACTIVE
        );

        Enrollment enrollment2 = createEnrollment(
                2L,
                course2,
                student,
                EnrollmentStatus.ACTIVE
        );

        given(userRepository.findById(studentId))
                .willReturn(Optional.of(student));
        given(enrollmentRepository.findByStudentAndStatus(student, EnrollmentStatus.ACTIVE))
                .willReturn(List.of(enrollment1, enrollment2));

        // when
        List<MyCourseResponse> responses = enrollmentService.findMyCourses(studentId);

        // then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getEnrollmentId());
        assertEquals(1L, responses.get(0).getCourseId());
        assertEquals("Java 기초 강좌", responses.get(0).getCourseTitle());
        assertEquals(2L, responses.get(1).getEnrollmentId());
        assertEquals(2L, responses.get(1).getCourseId());
        assertEquals("Spring 기초 강좌", responses.get(1).getCourseTitle());

        verify(userRepository).findById(studentId);
        verify(enrollmentRepository).findByStudentAndStatus(student, EnrollmentStatus.ACTIVE);
    }

    @Test
    @DisplayName("수강 신청을 취소하면 상태가 CANCELED로 변경되고 canceledAt이 기록된다.")
    void 수강_신청_취소_테스트() {

        // given
        Long studentId = 10L;
        Long enrollmentId = 1L;

        User student = createStudent(studentId);
        Course course = createCourse(1L, "Java 기초 강좌");

        Enrollment enrollment = createEnrollment(
                enrollmentId,
                course,
                student,
                EnrollmentStatus.ACTIVE
        );

        given(userRepository.findById(studentId))
                .willReturn(Optional.of(student));
        given(enrollmentRepository.findByEnrollmentIdAndStudentAndStatus(
                enrollmentId,
                student,
                EnrollmentStatus.ACTIVE
        )).willReturn(Optional.of(enrollment));

        // when
        enrollmentService.cancelEnrollment(studentId, enrollmentId);

        // then
        assertEquals(EnrollmentStatus.CANCELED, enrollment.getStatus());
        assertNotNull(enrollment.getCanceledAt());

        verify(userRepository).findById(studentId);
        verify(enrollmentRepository).findByEnrollmentIdAndStudentAndStatus(
                enrollmentId,
                student,
                EnrollmentStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("존재하지 않는 수강 신청 취소 시 EnrollmentNotFoundException이 발생한다.")
    void 존재하지_않는_수강_신청_취소_예외_테스트() {

        // given
        Long studentId = 10L;
        Long enrollmentId = 999L;

        User student = createStudent(studentId);

        given(userRepository.findById(studentId))
                .willReturn(Optional.of(student));
        given(enrollmentRepository.findByEnrollmentIdAndStudentAndStatus(
                enrollmentId,
                student,
                EnrollmentStatus.ACTIVE
        )).willReturn(Optional.empty());

        // when
        EnrollmentNotFoundException exception = assertThrows(
                EnrollmentNotFoundException.class,
                () -> enrollmentService.cancelEnrollment(studentId, enrollmentId)
        );

        // then
        assertEquals(enrollmentId, exception.getEnrollmentId());

        verify(userRepository).findById(studentId);
        verify(enrollmentRepository).findByEnrollmentIdAndStudentAndStatus(
                enrollmentId,
                student,
                EnrollmentStatus.ACTIVE
        );
    }

    private Course createCourse(Long courseId, String title) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setInstructorId(1L);
        course.setTitle(title);
        course.setDescription(title + " 설명입니다.");
        course.setThumbnailUrl("course.png");
        course.setCreatedAt(LocalDateTime.now());

        return course;
    }

    private User createStudent(Long studentId) {
        User student = mock(User.class);
        lenient().when(student.getUserId()).thenReturn(studentId);
        return student;
    }

    private Enrollment createEnrollment(
            Long enrollmentId,
            Course course,
            User student,
            EnrollmentStatus status
    ) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(enrollmentId);
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(status);
        enrollment.setEnrolledAt(LocalDateTime.now());

        return enrollment;
    }
}