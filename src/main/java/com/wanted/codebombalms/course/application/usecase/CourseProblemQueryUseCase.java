package com.wanted.codebombalms.course.application.usecase;

import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import java.util.List;

public interface CourseProblemQueryUseCase {

    List<CourseProblemSet> findProblemSetsByCourse(Long courseId);

    List<CourseProblemSet> findProblemSetsByCourseAndRole(Long courseId, CourseProblemSetRole role);

    List<CourseProblemSet> findProblemSetsByLecture(Long lectureId);

    CourseProblemSet findProblemSetById(Long courseProblemSetId);
}
