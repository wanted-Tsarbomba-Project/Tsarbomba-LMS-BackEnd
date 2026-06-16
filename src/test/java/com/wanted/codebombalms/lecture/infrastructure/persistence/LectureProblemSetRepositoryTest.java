package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseRepositoryAdapter;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.lecture.infrastructure.course.CourseCatalogAdapter;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
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

    @Autowired
    private SpringDataProblemSetRepository problemSetRepository;

    @Test
    void saveAndFindProblemSetsByCourse() {
        Course course = courseRepository.save(createCourse());
        Lecture mainLecture = lectureRepository.save(createLecture(course, "Java 1", 1, LectureStatus.ACTIVE));
        Long mainProblemSetId = createProblemSet("Main").getProblemSetId();
        Long finalProblemSetId = createProblemSet("Final").getProblemSetId();

        LectureProblemSet mainProblemSet = lectureProblemSetRepository.save(
                LectureProblemSet.create(
                        course.getCourseId(),
                        mainLecture.getLectureId(),
                        mainProblemSetId,
                        LectureProblemSetRole.MAIN,
                        1
                )
        );
        LectureProblemSet finalProblemSet = lectureProblemSetRepository.save(
                LectureProblemSet.create(
                        course.getCourseId(),
                        null,
                        finalProblemSetId,
                        LectureProblemSetRole.FINAL,
                        1
                )
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
        Long mainProblemSetId = createProblemSet("Main").getProblemSetId();
        Long finalProblemSetId = createProblemSet("Final").getProblemSetId();
        lectureProblemSetRepository.save(
                LectureProblemSet.create(
                        course.getCourseId(),
                        lecture.getLectureId(),
                        mainProblemSetId,
                        LectureProblemSetRole.MAIN,
                        2
                )
        );
        lectureProblemSetRepository.save(
                LectureProblemSet.create(
                        course.getCourseId(),
                        null,
                        finalProblemSetId,
                        LectureProblemSetRole.FINAL,
                        1
                )
        );

        List<LectureProblemSet> problemSets = lectureProblemSetRepository.findByLectureId(lecture.getLectureId());

        assertEquals(1, problemSets.size());
        assertEquals(2, problemSets.get(0).getDisplayOrder());
        assertEquals(mainProblemSetId, problemSets.get(0).getProblemSetId());
    }

    @Test
    void findByCourseId_excludesWhenCourseIsDeleted() {
        Course course = courseRepository.save(createCourse());
        Lecture lecture = lectureRepository.save(createLecture(course, "Java 1", 1, LectureStatus.ACTIVE));
        Long problemSetId = createProblemSet("Main").getProblemSetId();
        lectureProblemSetRepository.save(
                LectureProblemSet.create(
                        course.getCourseId(),
                        lecture.getLectureId(),
                        problemSetId,
                        LectureProblemSetRole.MAIN,
                        1
                )
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
        Long deletedProblemSetId = createProblemSet("Deleted lecture").getProblemSetId();
        Long remainingProblemSetId = createProblemSet("Active lecture").getProblemSetId();
        LectureProblemSet deletedProblemSet = lectureProblemSetRepository.save(
                LectureProblemSet.create(
                        course.getCourseId(),
                        deletedLecture.getLectureId(),
                        deletedProblemSetId,
                        LectureProblemSetRole.MAIN,
                        1
                )
        );
        LectureProblemSet remainingProblemSet = lectureProblemSetRepository.save(
                LectureProblemSet.create(
                        course.getCourseId(),
                        remainingLecture.getLectureId(),
                        remainingProblemSetId,
                        LectureProblemSetRole.MAIN,
                        1
                )
        );

        deletedLecture.delete();
        lectureRepository.save(deletedLecture);

        assertEquals(0, lectureProblemSetRepository.findByLectureId(deletedLecture.getLectureId()).size());
        assertEquals(0, lectureProblemSetRepository.findById(deletedProblemSet.getLectureProblemSetId()).stream().count());
        assertEquals(1, lectureProblemSetRepository.findByLectureId(remainingLecture.getLectureId()).size());
        assertEquals(remainingProblemSet.getLectureProblemSetId(), lectureProblemSetRepository.findByLectureId(remainingLecture.getLectureId()).get(0).getLectureProblemSetId());
    }

    @Test
    void findByCourseId_excludesDeletedProblemSetConnections() {
        Course course = courseRepository.save(createCourse());
        Lecture lecture = lectureRepository.save(createLecture(course, "Java 1", 1, LectureStatus.ACTIVE));
        ProblemSetJpaEntity problemSet = createProblemSet("Deleted problem set");
        lectureProblemSetRepository.save(
                LectureProblemSet.create(
                        course.getCourseId(),
                        lecture.getLectureId(),
                        problemSet.getProblemSetId(),
                        LectureProblemSetRole.MAIN,
                        1
                )
        );

        problemSet.deactivate();
        problemSetRepository.save(problemSet);

        assertEquals(0, lectureProblemSetRepository.findByCourseId(course.getCourseId()).size());
        assertEquals(0, lectureProblemSetRepository.findByLectureId(lecture.getLectureId()).size());
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

    private ProblemSetJpaEntity createProblemSet(String title) {
        return problemSetRepository.save(new ProblemSetJpaEntity(
                null,
                title,
                "description",
                "EASY",
                1,
                2L
        ));
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
