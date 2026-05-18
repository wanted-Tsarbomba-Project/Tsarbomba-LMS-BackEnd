package com.wanted.codebombalms.domain.course.repository;

import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.course.enums.CourseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("CourseRepository 테스트")
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("강좌를 저장하고 courseId로 조회할 수 있다.")
    void 강좌_저장_및_조회_테스트() {

        // given
        Course course = createCourse(
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE
        );

        Course savedCourse = courseRepository.save(course);

        // when
        Optional<Course> foundCourse =
                courseRepository.findByCourseIdAndDeletedAtIsNull(savedCourse.getCourseId());

        // then
        assertTrue(foundCourse.isPresent());
        assertEquals(savedCourse.getCourseId(), foundCourse.get().getCourseId());
        assertEquals("Java 기초 강좌", foundCourse.get().getTitle());
        assertEquals(10L, foundCourse.get().getInstructorId());
        assertEquals(CourseStatus.ACTIVE, foundCourse.get().getStatus());
        assertNull(foundCourse.get().getDeletedAt());
    }

    @Test
    @DisplayName("삭제되지 않은 강좌 목록만 조회할 수 있다.")
    void 삭제되지_않은_강좌_목록_조회_테스트() {

        // given
        Course activeCourse = createCourse(
                10L,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE
        );

        Course deletedCourse = createCourse(
                10L,
                "삭제된 강좌",
                "삭제된 강좌 설명입니다.",
                "deleted.png",
                CourseStatus.ACTIVE
        );
        deletedCourse.delete();

        courseRepository.save(activeCourse);
        courseRepository.save(deletedCourse);

        // when
        List<Course> courses = courseRepository.findByDeletedAtIsNull();

        // then
        assertFalse(courses.isEmpty());
        assertTrue(courses.stream().allMatch(course -> course.getDeletedAt() == null));
        assertTrue(courses.stream().anyMatch(course -> course.getTitle().equals("Java 기초 강좌")));
        assertFalse(courses.stream().anyMatch(course -> course.getTitle().equals("삭제된 강좌")));
    }

    @Test
    @DisplayName("강사 ID로 삭제되지 않은 강좌 목록을 조회할 수 있다.")
    void 강사_ID로_강좌_목록_조회_테스트() {

        // given
        Long instructorId = 10L;

        Course course1 = createCourse(
                instructorId,
                "Java 기초 강좌",
                "Java 기초 문법을 학습하는 강좌입니다.",
                "java.png",
                CourseStatus.ACTIVE
        );

        Course course2 = createCourse(
                instructorId,
                "Spring 기초 강좌",
                "Spring 기초를 학습하는 강좌입니다.",
                "spring.png",
                CourseStatus.ACTIVE
        );

        Course otherInstructorCourse = createCourse(
                20L,
                "다른 강사의 강좌",
                "다른 강사가 등록한 강좌입니다.",
                "other.png",
                CourseStatus.ACTIVE
        );

        courseRepository.save(course1);
        courseRepository.save(course2);
        courseRepository.save(otherInstructorCourse);

        // when
        List<Course> courses =
                courseRepository.findByInstructorIdAndDeletedAtIsNull(instructorId);

        // then
        assertEquals(2, courses.size());
        assertTrue(courses.stream().allMatch(course -> course.getInstructorId().equals(instructorId)));
        assertTrue(courses.stream().anyMatch(course -> course.getTitle().equals("Java 기초 강좌")));
        assertTrue(courses.stream().anyMatch(course -> course.getTitle().equals("Spring 기초 강좌")));
        assertFalse(courses.stream().anyMatch(course -> course.getTitle().equals("다른 강사의 강좌")));
    }

    private Course createCourse(
            Long instructorId,
            String title,
            String description,
            String thumbnailUrl,
            CourseStatus status
    ) {
        Course course = new Course();
        course.setInstructorId(instructorId);
        course.setTitle(title);
        course.setDescription(description);
        course.setThumbnailUrl(thumbnailUrl);
        course.setStatus(status);

        return course;
    }
}