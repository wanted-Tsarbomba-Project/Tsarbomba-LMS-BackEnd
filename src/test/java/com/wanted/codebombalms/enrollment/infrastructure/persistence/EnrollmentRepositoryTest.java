package com.wanted.codebombalms.enrollment.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseRepositoryAdapter;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.enrollment.infrastructure.persistence.EnrollmentRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({CourseRepositoryAdapter.class, EnrollmentRepositoryAdapter.class})
@DisplayName("EnrollmentRepository test")
class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void saveAndFindByEnrollmentIdAndStatus() {
        Course course = courseRepository.save(createCourse());
        Enrollment savedEnrollment = enrollmentRepository.save(Enrollment.create(10L, course.getCourseId()));

        Optional<Enrollment> foundEnrollment = enrollmentRepository.findByEnrollmentIdAndStatus(
                savedEnrollment.getEnrollmentId(),
                EnrollmentStatus.ACTIVE
        );

        assertTrue(foundEnrollment.isPresent());
        assertEquals(savedEnrollment.getEnrollmentId(), foundEnrollment.get().getEnrollmentId());
        assertEquals(10L, foundEnrollment.get().getUserId());
        assertEquals(course.getCourseId(), foundEnrollment.get().getCourseId());
        assertEquals(EnrollmentStatus.ACTIVE, foundEnrollment.get().getStatus());
        assertNotNull(foundEnrollment.get().getEnrolledAt());
        assertNull(foundEnrollment.get().getCanceledAt());
    }

    @Test
    void existsByCourseIdAndUserIdAndStatusReturnsTrueForActiveEnrollment() {
        Course course = courseRepository.save(createCourse());
        enrollmentRepository.save(Enrollment.create(10L, course.getCourseId()));

        boolean exists = enrollmentRepository.existsByCourseIdAndUserIdAndStatus(
                course.getCourseId(),
                10L,
                EnrollmentStatus.ACTIVE
        );

        assertTrue(exists);
    }

    @Test
    void existsByCourseIdAndUserIdReturnsTrueForCanceledEnrollment() {
        Course course = courseRepository.save(createCourse());
        Enrollment enrollment = Enrollment.create(10L, course.getCourseId());
        enrollment.cancel();
        enrollmentRepository.save(enrollment);

        boolean exists = enrollmentRepository.existsByCourseIdAndUserId(
                course.getCourseId(),
                10L
        );

        assertTrue(exists);
    }

    @Test
    void findByUserIdAndStatusReturnsOnlyMatchingStatus() {
        Course activeCourse = courseRepository.save(createCourse());
        Course canceledCourse = courseRepository.save(createCourse());
        Enrollment activeEnrollment = Enrollment.create(10L, activeCourse.getCourseId());
        Enrollment canceledEnrollment = Enrollment.create(10L, canceledCourse.getCourseId());
        canceledEnrollment.cancel();

        enrollmentRepository.save(activeEnrollment);
        enrollmentRepository.save(canceledEnrollment);

        List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(10L, EnrollmentStatus.ACTIVE);

        assertEquals(1, enrollments.size());
        assertEquals(EnrollmentStatus.ACTIVE, enrollments.get(0).getStatus());
        assertNull(enrollments.get(0).getCanceledAt());
    }

    @Test
    void saveCanceledEnrollmentPersistsCanceledAt() {
        Course course = courseRepository.save(createCourse());
        Enrollment enrollment = enrollmentRepository.save(Enrollment.create(10L, course.getCourseId()));

        enrollment.cancel();
        Enrollment canceledEnrollment = enrollmentRepository.save(enrollment);

        Optional<Enrollment> foundEnrollment = enrollmentRepository.findByEnrollmentIdAndUserIdAndStatus(
                canceledEnrollment.getEnrollmentId(),
                10L,
                EnrollmentStatus.CANCELED
        );

        assertTrue(foundEnrollment.isPresent());
        assertEquals(EnrollmentStatus.CANCELED, foundEnrollment.get().getStatus());
        assertNotNull(foundEnrollment.get().getCanceledAt());
    }

    private Course createCourse() {
        Course course = new Course();
        course.setInstructorId(20L);
        course.setTitle("Java");
        course.setDescription("description");
        course.setThumbnailUrl("java.png");
        course.setStatus(CourseStatus.ACTIVE);
        return course;
    }
}
