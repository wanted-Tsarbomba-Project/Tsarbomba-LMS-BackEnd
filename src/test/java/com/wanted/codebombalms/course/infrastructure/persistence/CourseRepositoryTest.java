package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseCategoryStatus;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseProblemSetRepository;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.enrollment.infrastructure.persistence.EnrollmentRepositoryAdapter;
import com.wanted.codebombalms.enrollment.infrastructure.persistence.SpringDataEnrollmentRepository;
import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import com.wanted.codebombalms.learning.infrastructure.persistence.LectureProblemProgressJpaEntity;
import com.wanted.codebombalms.learning.infrastructure.persistence.LectureProgressJpaEntity;
import com.wanted.codebombalms.learning.infrastructure.persistence.SpringDataLectureProblemProgressRepository;
import com.wanted.codebombalms.learning.infrastructure.persistence.SpringDataLectureProgressRepository;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.lecture.infrastructure.course.CourseCatalogAdapter;
import com.wanted.codebombalms.lecture.infrastructure.persistence.LectureRepositoryAdapter;
import com.wanted.codebombalms.lecture.infrastructure.persistence.SpringDataLectureRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({
        CourseRepositoryAdapter.class,
        CourseCatalogAdapter.class,
        LectureRepositoryAdapter.class,
        CourseProblemSetRepositoryAdapter.class,
        EnrollmentRepositoryAdapter.class
})
@DisplayName("CourseRepository 테스트")
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SpringDataCourseCategoryRepository springDataCourseCategoryRepository;

    @Autowired
    private SpringDataCourseRepository springDataCourseRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private SpringDataLectureRepository springDataLectureRepository;

    @Autowired
    private CourseProblemSetRepository courseProblemSetRepository;

    @Autowired
    private SpringDataEnrollmentRepository springDataEnrollmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SpringDataLectureProgressRepository lectureProgressRepository;

    @Autowired
    private SpringDataLectureProblemProgressRepository lectureProblemProgressRepository;

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

    @Test
    void findByCourseCategoryIdAndStatusAndDeletedAtIsNullReturnsMatchingCourses() {
        CourseCategoryJpaEntity category = springDataCourseCategoryRepository.save(
                new CourseCategoryJpaEntity("Python", CourseCategoryStatus.ACTIVE, 1)
        );

        Course course = createCourse(
                10L,
                "Python",
                "Python course",
                "python.png",
                CourseStatus.ACTIVE
        );
        course.setCourseCategoryId(category.getCourseCategoryId());

        courseRepository.save(course);

        List<Course> courses = courseRepository.findByCourseCategoryIdAndStatusAndDeletedAtIsNull(
                category.getCourseCategoryId(),
                CourseStatus.ACTIVE
        );

        assertEquals(1, courses.size());
        assertEquals(category.getCourseCategoryId(), courses.get(0).getCourseCategoryId());
        assertEquals("Python", courses.get(0).getCourseCategoryName());
    }

    @Test
    void hardDeleteByDeletedAtBefore_deletesOldSoftDeletedCourseWithDependentsOnly() {
        LocalDateTime threshold = LocalDateTime.of(2026, 5, 1, 0, 0);
        Course oldDeletedCourse = createCourse(10L, "Old Deleted", "description", "old.png", CourseStatus.DELETED);
        oldDeletedCourse.setDeletedAt(threshold.minusDays(1));
        Course recentDeletedCourse = createCourse(10L, "Recent Deleted", "description", "recent.png", CourseStatus.DELETED);
        recentDeletedCourse.setDeletedAt(threshold.plusDays(1));
        Course activeCourse = createCourse(10L, "Active", "description", "active.png", CourseStatus.ACTIVE);

        oldDeletedCourse = courseRepository.save(oldDeletedCourse);
        recentDeletedCourse = courseRepository.save(recentDeletedCourse);
        activeCourse = courseRepository.save(activeCourse);

        Lecture oldLecture = lectureRepository.save(
                Lecture.create(oldDeletedCourse, "Old Lecture", "description", "old.mp4", "old.png", 1, LectureStatus.DELETED)
        );
        CourseProblemSet oldProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(oldDeletedCourse.getCourseId(), oldLecture.getLectureId(), 2002L, CourseProblemSetRole.MAIN, 1)
        );
        enrollmentRepository.save(Enrollment.create(1L, oldDeletedCourse.getCourseId()));
        lectureProgressRepository.save(LectureProgressJpaEntity.from(
                LectureProgress.create(1L, oldLecture.getLectureId())
        ));
        lectureProblemProgressRepository.save(LectureProblemProgressJpaEntity.from(
                LectureProblemProgress.create(1L, oldProblemSet.getCourseProblemSetId())
        ));

        int deletedCount = springDataCourseRepository.hardDeleteByDeletedAtBefore(threshold);

        assertEquals(1, deletedCount);
        assertFalse(springDataCourseRepository.findById(oldDeletedCourse.getCourseId()).isPresent());
        assertTrue(springDataCourseRepository.findById(recentDeletedCourse.getCourseId()).isPresent());
        assertTrue(springDataCourseRepository.findById(activeCourse.getCourseId()).isPresent());
        assertFalse(springDataLectureRepository.findById(oldLecture.getLectureId()).isPresent());
        assertEquals(0, courseProblemSetRepository.findByCourseId(oldDeletedCourse.getCourseId()).size());
        assertEquals(0, springDataEnrollmentRepository.findAll().size());
        assertEquals(0, lectureProgressRepository.findAll().size());
        assertEquals(0, lectureProblemProgressRepository.findAll().size());
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
