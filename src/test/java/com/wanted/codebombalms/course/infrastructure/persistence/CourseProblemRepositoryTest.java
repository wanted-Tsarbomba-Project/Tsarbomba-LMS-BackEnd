package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseProblemSetRepository;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseProblemSetRepositoryAdapter;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseRepositoryAdapter;
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
        CourseProblemSetRepositoryAdapter.class
})
@DisplayName("Course problem repository test")
class CourseProblemRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseProblemSetRepository courseProblemSetRepository;

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
