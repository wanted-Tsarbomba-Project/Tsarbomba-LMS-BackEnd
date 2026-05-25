package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import com.wanted.codebombalms.course.domain.model.CourseProblemStep;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseProblemSetRepository;
import com.wanted.codebombalms.course.domain.repository.CourseProblemStepRepository;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseProblemSetRepositoryAdapter;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseProblemStepRepositoryAdapter;
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
        CourseProblemSetRepositoryAdapter.class,
        CourseProblemStepRepositoryAdapter.class
})
@DisplayName("Course problem repository test")
class CourseProblemRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseProblemSetRepository courseProblemSetRepository;

    @Autowired
    private CourseProblemStepRepository courseProblemStepRepository;

    @Test
    void saveAndFindProblemSetsByCourse() {
        Course course = courseRepository.save(createCourse());

        CourseProblemSet mainProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 2002L, CourseProblemSetRole.MAIN)
        );
        courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 2003L, CourseProblemSetRole.FINAL)
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
    void saveAndFindProblemStepsByLecture() {
        Course course = courseRepository.save(createCourse());
        CourseProblemSet mainProblemSet = courseProblemSetRepository.save(
                CourseProblemSet.create(course.getCourseId(), 2002L, CourseProblemSetRole.MAIN)
        );

        courseProblemStepRepository.save(
                CourseProblemStep.create(mainProblemSet.getCourseProblemSetId(), 2004L, 101L, 2L)
        );
        courseProblemStepRepository.save(
                CourseProblemStep.create(mainProblemSet.getCourseProblemSetId(), 2005L, 101L, 1L)
        );

        List<CourseProblemStep> steps = courseProblemStepRepository.findByLectureId(101L);

        assertEquals(2, steps.size());
        assertEquals(1L, steps.get(0).getStepOrder());
        assertEquals(2005L, steps.get(0).getProblemId());
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
