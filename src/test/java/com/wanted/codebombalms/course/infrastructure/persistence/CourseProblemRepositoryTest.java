package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseProblemSetRepository;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.lecture.infrastructure.course.CourseCatalogAdapter;
import com.wanted.codebombalms.lecture.infrastructure.persistence.LectureRepositoryAdapter;
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
        CourseProblemSetRepositoryAdapter.class,
        CourseCatalogAdapter.class,
        LectureRepositoryAdapter.class
})
@DisplayName("Course problem repository test")
class CourseProblemRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseProblemSetRepository courseProblemSetRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Test
    void saveAndFindProblemSetsByCourse() {
        Course course = courseRepository.save(createCourse());
        Lecture mainLecture = lectureRepository.save(createLecture(course, "Java 1", 1, LectureStatus.ACTIVE));

        CourseProblemSet mainProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), mainLecture.getLectureId(), 2002L, CourseProblemSetRole.MAIN, 1)
        );
        CourseProblemSet finalProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), null, 2003L, CourseProblemSetRole.FINAL, 1)
        );

        List<CourseProblemSet> allProblemSets = courseProblemSetRepository.findByCourseId(course.getCourseId());
        List<CourseProblemSet> mainProblemSets = courseProblemSetRepository.findByCourseIdAndRole(
                course.getCourseId(),
                CourseProblemSetRole.MAIN
        );

        assertEquals(2, allProblemSets.size());
        assertEquals(1, mainProblemSets.size());
        assertEquals(mainProblemSet.getCourseProblemSetId(), mainProblemSets.get(0).getCourseProblemSetId());
        assertEquals(
                finalProblemSet.getCourseProblemSetId(),
                courseProblemSetRepository.findById(finalProblemSet.getCourseProblemSetId())
                        .orElseThrow()
                        .getCourseProblemSetId()
        );
    }

    @Test
    void saveAndFindProblemSetsByLecture() {
        Course course = courseRepository.save(createCourse());
        Lecture lecture = lectureRepository.save(createLecture(course, "Java 1", 1, LectureStatus.ACTIVE));
        courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), lecture.getLectureId(), 2002L, CourseProblemSetRole.MAIN, 2)
        );
        courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), null, 2003L, CourseProblemSetRole.FINAL, 1)
        );

        List<CourseProblemSet> problemSets = courseProblemSetRepository.findByLectureId(lecture.getLectureId());

        assertEquals(1, problemSets.size());
        assertEquals(2, problemSets.get(0).getDisplayOrder());
        assertEquals(2002L, problemSets.get(0).getProblemSetId());
    }

    @Test
    void findByCourseId_excludesWhenCourseIsDeleted() {
        Course course = courseRepository.save(createCourse());
        Lecture lecture = lectureRepository.save(createLecture(course, "Java 1", 1, LectureStatus.ACTIVE));
        courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), lecture.getLectureId(), 2002L, CourseProblemSetRole.MAIN, 1)
        );

        course.delete();
        courseRepository.save(course);

        assertEquals(0, courseProblemSetRepository.findByCourseId(course.getCourseId()).size());
        assertEquals(0, courseProblemSetRepository.findByLectureId(lecture.getLectureId()).size());
    }

    @Test
    void findByLectureId_excludesOnlyDeletedLectureProblemSets() {
        Course course = courseRepository.save(createCourse());
        Lecture deletedLecture = lectureRepository.save(createLecture(course, "Deleted", 1, LectureStatus.ACTIVE));
        Lecture remainingLecture = lectureRepository.save(createLecture(course, "Active", 2, LectureStatus.ACTIVE));
        CourseProblemSet deletedProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), deletedLecture.getLectureId(), 2002L, CourseProblemSetRole.MAIN, 1)
        );
        CourseProblemSet remainingProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), remainingLecture.getLectureId(), 2003L, CourseProblemSetRole.MAIN, 1)
        );

        deletedLecture.delete();
        lectureRepository.save(deletedLecture);

        assertEquals(0, courseProblemSetRepository.findByLectureId(deletedLecture.getLectureId()).size());
        assertEquals(0, courseProblemSetRepository.findById(deletedProblemSet.getCourseProblemSetId()).stream().count());
        assertEquals(1, courseProblemSetRepository.findByLectureId(remainingLecture.getLectureId()).size());
        assertEquals(remainingProblemSet.getCourseProblemSetId(), courseProblemSetRepository.findByLectureId(remainingLecture.getLectureId()).get(0).getCourseProblemSetId());
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
