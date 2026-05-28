package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseProblemSetRepository;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseProblemSetRepositoryAdapter;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseRepositoryAdapter;
import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import com.wanted.codebombalms.learning.infrastructure.persistence.LectureProblemProgressJpaEntity;
import com.wanted.codebombalms.learning.infrastructure.persistence.SpringDataLectureProblemProgressRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({
        CourseRepositoryAdapter.class,
        CourseProblemSetRepositoryAdapter.class
})
@DisplayName("Course problem repository test")
class CourseProblemRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseProblemSetRepository courseProblemSetRepository;

    @Autowired
    private SpringDataCourseProblemSetRepository springDataCourseProblemSetRepository;

    @Autowired
    private SpringDataLectureProblemProgressRepository lectureProblemProgressRepository;

    @Test
    void saveAndFindProblemSetsByCourse() {
        Course course = courseRepository.save(createCourse());

        CourseProblemSet mainProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 101L, 2002L, CourseProblemSetRole.MAIN, 1)
        );
        courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 103L, 2003L, CourseProblemSetRole.FINAL, 1)
        );

        List<CourseProblemSet> allProblemSets = courseProblemSetRepository.findByCourseId(course.getCourseId());
        List<CourseProblemSet> mainProblemSets = courseProblemSetRepository.findByCourseIdAndRole(
                course.getCourseId(),
                CourseProblemSetRole.MAIN
        );

        assertEquals(2, allProblemSets.size());
        assertEquals(1, mainProblemSets.size());
        assertEquals(mainProblemSet.getCourseProblemSetId(), mainProblemSets.get(0).getCourseProblemSetId());
    }

    @Test
    void saveAndFindProblemSetsByLecture() {
        Course course = courseRepository.save(createCourse());
        courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 101L, 2002L, CourseProblemSetRole.MAIN, 2)
        );
        courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 101L, 2003L, CourseProblemSetRole.FINAL, 1)
        );

        List<CourseProblemSet> problemSets = courseProblemSetRepository.findByLectureId(101L);

        assertEquals(2, problemSets.size());
        assertEquals(1, problemSets.get(0).getDisplayOrder());
        assertEquals(2003L, problemSets.get(0).getProblemSetId());
    }

    @Test
    void deleteByCourseId_softDeletesProblemSets() {
        Course course = courseRepository.save(createCourse());
        CourseProblemSet problemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 101L, 2002L, CourseProblemSetRole.MAIN, 1)
        );

        courseProblemSetRepository.deleteByCourseId(course.getCourseId());

        assertEquals(0, courseProblemSetRepository.findByCourseId(course.getCourseId()).size());
        assertEquals(0, courseProblemSetRepository.findByLectureId(101L).size());
        assertEquals(0, courseProblemSetRepository.findById(problemSet.getCourseProblemSetId()).stream().count());
    }

    @Test
    void deleteByLectureId_softDeletesOnlyMatchingLectureProblemSets() {
        Course course = courseRepository.save(createCourse());
        CourseProblemSet deletedProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 101L, 2002L, CourseProblemSetRole.MAIN, 1)
        );
        CourseProblemSet remainingProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 102L, 2003L, CourseProblemSetRole.FINAL, 1)
        );

        courseProblemSetRepository.deleteByLectureId(101L);

        assertEquals(0, courseProblemSetRepository.findByLectureId(101L).size());
        assertEquals(0, courseProblemSetRepository.findById(deletedProblemSet.getCourseProblemSetId()).stream().count());
        assertEquals(1, courseProblemSetRepository.findByLectureId(102L).size());
        assertEquals(remainingProblemSet.getCourseProblemSetId(), courseProblemSetRepository.findByLectureId(102L).get(0).getCourseProblemSetId());
    }

    @Test
    void hardDeleteByDeletedAtBefore_deletesOldSoftDeletedProblemSetsOnly() {
        Course course = courseRepository.save(createCourse());
        LocalDateTime threshold = LocalDateTime.of(2026, 5, 1, 0, 0);
        CourseProblemSet oldDeletedProblemSet = courseProblemSetRepository.save(CourseProblemSet.restore(
                null,
                course.getCourseId(),
                101L,
                2002L,
                CourseProblemSetRole.MAIN,
                1,
                threshold.minusDays(1)
        ));
        CourseProblemSet recentDeletedProblemSet = courseProblemSetRepository.save(CourseProblemSet.restore(
                null,
                course.getCourseId(),
                102L,
                2003L,
                CourseProblemSetRole.FINAL,
                1,
                threshold.plusDays(1)
        ));
        CourseProblemSet activeProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 103L, 2004L, CourseProblemSetRole.MAIN, 1)
        );
        lectureProblemProgressRepository.save(LectureProblemProgressJpaEntity.from(
                LectureProblemProgress.create(1L, oldDeletedProblemSet.getCourseProblemSetId())
        ));

        int deletedCount = springDataCourseProblemSetRepository.hardDeleteByDeletedAtBefore(threshold);

        assertEquals(1, deletedCount);
        assertFalse(springDataCourseProblemSetRepository.findById(oldDeletedProblemSet.getCourseProblemSetId()).isPresent());
        assertTrue(springDataCourseProblemSetRepository.findById(recentDeletedProblemSet.getCourseProblemSetId()).isPresent());
        assertTrue(springDataCourseProblemSetRepository.findById(activeProblemSet.getCourseProblemSetId()).isPresent());
        assertEquals(0, lectureProblemProgressRepository.findAll().size());
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
}
