package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseRepositoryAdapter;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.lecture.infrastructure.course.CourseCatalogAdapter;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import com.wanted.codebombalms.lecture.domain.repository.LectureProblemSetRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({
        CourseRepositoryAdapter.class,
        LectureProblemSetRepositoryAdapter.class,
        CourseCatalogAdapter.class,
        LectureRepositoryAdapter.class
})
@DisplayName("Lecture problem set repository test")
class LectureProblemSetRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LectureProblemSetRepository lectureProblemSetRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Test
    void saveAndFindProblemSetsByCourse() {
        Course course = courseRepository.save(createCourse());
        Lecture mainLecture = lectureRepository.save(createLecture(course, "Java 1", 1, LectureStatus.ACTIVE));

        LectureProblemSet mainProblemSet = lectureProblemSetRepository.save(
                LectureProblemSet.create(course.getCourseId(), mainLecture.getLectureId(), 2002L, LectureProblemSetRole.MAIN, 1)
        );
        LectureProblemSet finalProblemSet = lectureProblemSetRepository.save(
                LectureProblemSet.create(course.getCourseId(), null, 2003L, LectureProblemSetRole.FINAL, 1)
        );

        List<LectureProblemSet> allProblemSets = lectureProblemSetRepository.findByCourseId(course.getCourseId());
        List<LectureProblemSet> mainProblemSets = lectureProblemSetRepository.findByCourseIdAndRole(
                course.getCourseId(),
                LectureProblemSetRole.MAIN
        );

        assertEquals(2, allProblemSets.size());
        assertEquals(1, mainProblemSets.size());
        assertEquals(mainProblemSet.getLectureProblemSetId(), mainProblemSets.get(0).getLectureProblemSetId());
        assertEquals(
                finalProblemSet.getLectureProblemSetId(),
                lectureProblemSetRepository.findById(finalProblemSet.getLectureProblemSetId())
                        .orElseThrow()
                        .getLectureProblemSetId()
        );
    }

    @Test
    void saveAndFindProblemSetsByLecture() {
        Course course = courseRepository.save(createCourse());
        Lecture lecture = lectureRepository.save(createLecture(course, "Java 1", 1, LectureStatus.ACTIVE));
        lectureProblemSetRepository.save(
                LectureProblemSet.create(course.getCourseId(), lecture.getLectureId(), 2002L, LectureProblemSetRole.MAIN, 2)
        );
        lectureProblemSetRepository.save(
                LectureProblemSet.create(course.getCourseId(), null, 2003L, LectureProblemSetRole.FINAL, 1)
        );

        List<LectureProblemSet> problemSets = lectureProblemSetRepository.findByLectureId(lecture.getLectureId());

        assertEquals(1, problemSets.size());
        assertEquals(2, problemSets.get(0).getDisplayOrder());
        assertEquals(2002L, problemSets.get(0).getProblemSetId());
    }

    @Test
    void findByCourseId_excludesWhenCourseIsDeleted() {
        Course course = courseRepository.save(createCourse());
        Lecture lecture = lectureRepository.save(createLecture(course, "Java 1", 1, LectureStatus.ACTIVE));
        lectureProblemSetRepository.save(
                LectureProblemSet.create(course.getCourseId(), lecture.getLectureId(), 2002L, LectureProblemSetRole.MAIN, 1)
        );

        course.delete();
        courseRepository.save(course);

        assertEquals(0, lectureProblemSetRepository.findByCourseId(course.getCourseId()).size());
        assertEquals(0, lectureProblemSetRepository.findByLectureId(lecture.getLectureId()).size());
    }

    @Test
    void findByLectureId_excludesOnlyDeletedLectureProblemSets() {
        Course course = courseRepository.save(createCourse());
        Lecture deletedLecture = lectureRepository.save(createLecture(course, "Deleted", 1, LectureStatus.ACTIVE));
        Lecture remainingLecture = lectureRepository.save(createLecture(course, "Active", 2, LectureStatus.ACTIVE));
        LectureProblemSet deletedProblemSet = lectureProblemSetRepository.save(
                LectureProblemSet.create(course.getCourseId(), deletedLecture.getLectureId(), 2002L, LectureProblemSetRole.MAIN, 1)
        );
        LectureProblemSet remainingProblemSet = lectureProblemSetRepository.save(
                LectureProblemSet.create(course.getCourseId(), remainingLecture.getLectureId(), 2003L, LectureProblemSetRole.MAIN, 1)
        );

        deletedLecture.delete();
        lectureRepository.save(deletedLecture);

        assertEquals(0, lectureProblemSetRepository.findByLectureId(deletedLecture.getLectureId()).size());
        assertEquals(0, lectureProblemSetRepository.findById(deletedProblemSet.getLectureProblemSetId()).stream().count());
        assertEquals(1, lectureProblemSetRepository.findByLectureId(remainingLecture.getLectureId()).size());
        assertEquals(remainingProblemSet.getLectureProblemSetId(), lectureProblemSetRepository.findByLectureId(remainingLecture.getLectureId()).get(0).getLectureProblemSetId());
    }

    private Course createCourse() {
        Course course = new Course();
        course.setInstructorId(2L);
        course.setTitle("Java");
        course.setDescription("description");
        course.setThumbnailUrl("java.png");
        course.setStatus(CourseStatus.ACTIVE);
        return course;
    }

    private Lecture createLecture(Course course, String title, Integer lectureOrder, LectureStatus status) {
        return Lecture.create(
                course,
                title,
                "description",
                "video.mp4",
                "lecture.png",
                lectureOrder,
                status
        );
    }
}
