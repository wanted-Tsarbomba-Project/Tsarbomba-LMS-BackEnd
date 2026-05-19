package com.wanted.codebombalms.domain.lecture.repository;

import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.course.enums.CourseStatus;
import com.wanted.codebombalms.domain.course.repository.CourseRepository;
import com.wanted.codebombalms.domain.lecture.entity.Lecture;
import com.wanted.codebombalms.domain.lecture.enums.LectureStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("LectureRepository 테스트")
class LectureRepositoryTest {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("강의를 저장하고 lectureId로 조회할 수 있다.")
    void 강의_저장_및_조회_테스트() {

        // given
        Course course = courseRepository.save(createCourse());

        Lecture lecture = Lecture.create(
                course,
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                1,
                LectureStatus.ACTIVE
        );

        Lecture savedLecture = lectureRepository.save(lecture);

        // when
        Optional<Lecture> foundLecture =
                lectureRepository.findByLectureIdAndDeletedAtIsNull(savedLecture.getLectureId());

        // then
        assertTrue(foundLecture.isPresent());
        assertEquals(savedLecture.getLectureId(), foundLecture.get().getLectureId());
        assertEquals("Java 1강", foundLecture.get().getTitle());
        assertEquals("java-1.mp4", foundLecture.get().getVideoUrl());
        assertEquals(1, foundLecture.get().getLectureOrder());
        assertEquals(LectureStatus.ACTIVE, foundLecture.get().getStatus());
        assertNull(foundLecture.get().getDeletedAt());
    }

    @Test
    @DisplayName("특정 강좌의 강의 목록을 lectureOrder 오름차순으로 조회할 수 있다.")
    void 강좌_ID로_강의_목록_조회_테스트() {

        // given
        Course course = courseRepository.save(createCourse());

        Lecture lecture2 = Lecture.create(
                course,
                "Java 2강",
                "Java 객체지향을 학습하는 강의입니다.",
                "java-2.mp4",
                "java-2.png",
                2,
                LectureStatus.ACTIVE
        );

        Lecture lecture1 = Lecture.create(
                course,
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                1,
                LectureStatus.ACTIVE
        );

        lectureRepository.save(lecture2);
        lectureRepository.save(lecture1);

        // when
        List<Lecture> lectures =
                lectureRepository.findByCourse_CourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(course.getCourseId());

        // then
        assertEquals(2, lectures.size());
        assertEquals("Java 1강", lectures.get(0).getTitle());
        assertEquals(1, lectures.get(0).getLectureOrder());
        assertEquals("Java 2강", lectures.get(1).getTitle());
        assertEquals(2, lectures.get(1).getLectureOrder());
    }

    @Test
    @DisplayName("삭제되지 않은 강의 목록만 조회할 수 있다.")
    void 삭제되지_않은_강의_목록_조회_테스트() {

        // given
        Course course = courseRepository.save(createCourse());

        Lecture activeLecture = Lecture.create(
                course,
                "Java 1강",
                "Java 기본 문법을 학습하는 강의입니다.",
                "java-1.mp4",
                "java-1.png",
                1,
                LectureStatus.ACTIVE
        );

        Lecture deletedLecture = Lecture.create(
                course,
                "삭제된 강의",
                "삭제 처리된 강의입니다.",
                "deleted.mp4",
                "deleted.png",
                2,
                LectureStatus.ACTIVE
        );

        deletedLecture.delete();

        lectureRepository.save(activeLecture);
        lectureRepository.save(deletedLecture);

        // when
        List<Lecture> lectures = lectureRepository.findByDeletedAtIsNull();

        // then
        assertFalse(lectures.isEmpty());
        assertTrue(lectures.stream().allMatch(lecture -> lecture.getDeletedAt() == null));
        assertTrue(lectures.stream().anyMatch(lecture -> lecture.getTitle().equals("Java 1강")));
        assertFalse(lectures.stream().anyMatch(lecture -> lecture.getTitle().equals("삭제된 강의")));
    }

    private Course createCourse() {
        Course course = new Course();
        course.setInstructorId(10L);
        course.setTitle("Java 기초 강좌");
        course.setDescription("Java 기초 문법을 학습하는 강좌입니다.");
        course.setThumbnailUrl("java-course.png");
        course.setStatus(CourseStatus.ACTIVE);

        return course;
    }
}