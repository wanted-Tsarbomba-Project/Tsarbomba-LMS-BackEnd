package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseRepositoryAdapter;
import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import com.wanted.codebombalms.learning.infrastructure.persistence.LectureProblemProgressJpaEntity;
import com.wanted.codebombalms.learning.infrastructure.persistence.LectureProblemSubmissionJpaEntity;
import com.wanted.codebombalms.learning.infrastructure.persistence.LectureProgressJpaEntity;
import com.wanted.codebombalms.learning.infrastructure.persistence.SpringDataLectureProblemProgressRepository;
import com.wanted.codebombalms.learning.infrastructure.persistence.SpringDataLectureProblemSubmissionRepository;
import com.wanted.codebombalms.learning.infrastructure.persistence.SpringDataLectureProgressRepository;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureProblemSetRepository;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.lecture.infrastructure.course.CourseCatalogAdapter;
import com.wanted.codebombalms.lecture.infrastructure.persistence.LectureRepositoryAdapter;
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
        LectureProblemSetRepositoryAdapter.class
})
@DisplayName("LectureRepository test")
class LectureRepositoryTest {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LectureProblemSetRepository lectureProblemSetRepository;

    @Autowired
    private SpringDataLectureRepository springDataLectureRepository;

    @Autowired
    private SpringDataLectureProgressRepository lectureProgressRepository;

    @Autowired
    private SpringDataLectureProblemProgressRepository lectureProblemProgressRepository;

    @Autowired
    private SpringDataLectureProblemSubmissionRepository lectureProblemSubmissionRepository;

    @Test
    void saveAndFindByLectureId() {
        Course course = courseRepository.save(createCourse());
        Lecture savedLecture = lectureRepository.save(Lecture.create(
                course,
                "Java 1",
                "description",
                "java-1.mp4",
                "java-1.png",
                null,
                1,
                LectureStatus.ACTIVE
        ));

        Optional<Lecture> foundLecture =
                lectureRepository.findByLectureIdAndDeletedAtIsNull(savedLecture.getLectureId());

        assertTrue(foundLecture.isPresent());
        assertEquals(savedLecture.getLectureId(), foundLecture.get().getLectureId());
        assertEquals("Java 1", foundLecture.get().getTitle());
        assertEquals(1, foundLecture.get().getLectureOrder());
    }

    @Test
    void findByCourseIdOrdersByLectureOrder() {
        Course course = courseRepository.save(createCourse());

        lectureRepository.save(Lecture.create(course, "Java 2", "description", "java-2.mp4", "java-2.png", null, 2, LectureStatus.ACTIVE));
        lectureRepository.save(Lecture.create(course, "Java 1", "description", "java-1.mp4", "java-1.png", null, 1, LectureStatus.ACTIVE));

        List<Lecture> lectures =
                lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(course.getCourseId());

        assertEquals(2, lectures.size());
        assertEquals("Java 1", lectures.get(0).getTitle());
        assertEquals("Java 2", lectures.get(1).getTitle());
    }

    @Test
    void findByDeletedAtIsNullExcludesDeletedLectures() {
        Course course = courseRepository.save(createCourse());
        Lecture activeLecture = Lecture.create(course, "Java 1", "description", "java-1.mp4", "java-1.png", null, 1, LectureStatus.ACTIVE);
        Lecture deletedLecture = Lecture.create(course, "Deleted", "description", "deleted.mp4", "deleted.png", null, 2, LectureStatus.ACTIVE);
        deletedLecture.delete();

        lectureRepository.save(activeLecture);
        lectureRepository.save(deletedLecture);

        List<Lecture> lectures = lectureRepository.findByDeletedAtIsNull();

        assertTrue(lectures.stream().allMatch(lecture -> lecture.getDeletedAt() == null));
        assertTrue(lectures.stream().anyMatch(lecture -> lecture.getTitle().equals("Java 1")));
        assertFalse(lectures.stream().anyMatch(lecture -> lecture.getTitle().equals("Deleted")));
    }

    @Test
    void hardDeleteByDeletedAtBefore_deletesOldSoftDeletedLectureOnly() {
        Course course = courseRepository.save(createCourse());
        LocalDateTime threshold = LocalDateTime.of(2026, 5, 1, 0, 0);
        Lecture oldDeletedLecture = lectureRepository.save(Lecture.restore(
                null,
                course,
                "Old Deleted",
                "description",
                "old.mp4",
                "old.png",
                null,
                LectureStatus.DELETED,
                null,
                null,
                threshold.minusDays(1),
                1
        ));
        Lecture recentDeletedLecture = lectureRepository.save(Lecture.restore(
                null,
                course,
                "Recent Deleted",
                "description",
                "recent.mp4",
                "recent.png",
                null,
                LectureStatus.DELETED,
                null,
                null,
                threshold.plusDays(1),
                2
        ));
        Lecture activeLecture = lectureRepository.save(
                Lecture.create(course, "Active", "description", "active.mp4", "active.png", null, 3, LectureStatus.ACTIVE)
        );
        LectureProblemSet lectureProblemSet = lectureProblemSetRepository.save(
                LectureProblemSet.create(course.getCourseId(), oldDeletedLecture.getLectureId(), 2002L, LectureProblemSetRole.MAIN, 1)
        );
        lectureProgressRepository.save(LectureProgressJpaEntity.from(
                LectureProgress.create(1L, oldDeletedLecture.getLectureId())
        ));
        lectureProblemProgressRepository.save(LectureProblemProgressJpaEntity.from(
                LectureProblemProgress.create(1L, lectureProblemSet.getLectureProblemSetId())
        ));
        lectureProblemSubmissionRepository.save(LectureProblemSubmissionJpaEntity.from(
                LectureProblemSubmission.create(
                        1L,
                        lectureProblemSet.getLectureProblemSetId(),
                        3001L,
                        "answer",
                        true,
                        1,
                        1,
                        1,
                        "SUCCESS",
                        null
                )
        ));

        int deletedCount = springDataLectureRepository.hardDeleteByDeletedAtBefore(threshold);

        assertEquals(1, deletedCount);
        assertFalse(springDataLectureRepository.findById(oldDeletedLecture.getLectureId()).isPresent());
        assertTrue(springDataLectureRepository.findById(recentDeletedLecture.getLectureId()).isPresent());
        assertTrue(springDataLectureRepository.findById(activeLecture.getLectureId()).isPresent());
        assertEquals(0, lectureProblemSetRepository.findByLectureId(oldDeletedLecture.getLectureId()).size());
        assertEquals(0, lectureProgressRepository.findAll().size());
        assertEquals(0, lectureProblemProgressRepository.findAll().size());
        assertEquals(0, lectureProblemSubmissionRepository.findAll().size());
    }

    private Course createCourse() {
        Course course = new Course();
        course.setInstructorId(10L);
        course.setTitle("Java");
        course.setDescription("description");
        course.setThumbnailUrl("java-course.png");
        course.setStatus(CourseStatus.ACTIVE);
        return course;
    }
}
