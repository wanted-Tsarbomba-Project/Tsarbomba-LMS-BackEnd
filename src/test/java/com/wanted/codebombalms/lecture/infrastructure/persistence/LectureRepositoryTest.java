package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseRepositoryAdapter;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.lecture.infrastructure.persistence.LectureRepositoryAdapter;
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
@Import({CourseRepositoryAdapter.class, LectureRepositoryAdapter.class})
@DisplayName("LectureRepository test")
class LectureRepositoryTest {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void saveAndFindByLectureId() {
        Course course = courseRepository.save(createCourse());
        Lecture savedLecture = lectureRepository.save(Lecture.create(
                course,
                "Java 1",
                "description",
                "java-1.mp4",
                "java-1.png",
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

        lectureRepository.save(Lecture.create(course, "Java 2", "description", "java-2.mp4", "java-2.png", 2, LectureStatus.ACTIVE));
        lectureRepository.save(Lecture.create(course, "Java 1", "description", "java-1.mp4", "java-1.png", 1, LectureStatus.ACTIVE));

        List<Lecture> lectures =
                lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(course.getCourseId());

        assertEquals(2, lectures.size());
        assertEquals("Java 1", lectures.get(0).getTitle());
        assertEquals("Java 2", lectures.get(1).getTitle());
    }

    @Test
    void findByDeletedAtIsNullExcludesDeletedLectures() {
        Course course = courseRepository.save(createCourse());
        Lecture activeLecture = Lecture.create(course, "Java 1", "description", "java-1.mp4", "java-1.png", 1, LectureStatus.ACTIVE);
        Lecture deletedLecture = Lecture.create(course, "Deleted", "description", "deleted.mp4", "deleted.png", 2, LectureStatus.ACTIVE);
        deletedLecture.delete();

        lectureRepository.save(activeLecture);
        lectureRepository.save(deletedLecture);

        List<Lecture> lectures = lectureRepository.findByDeletedAtIsNull();

        assertTrue(lectures.stream().allMatch(lecture -> lecture.getDeletedAt() == null));
        assertTrue(lectures.stream().anyMatch(lecture -> lecture.getTitle().equals("Java 1")));
        assertFalse(lectures.stream().anyMatch(lecture -> lecture.getTitle().equals("Deleted")));
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
